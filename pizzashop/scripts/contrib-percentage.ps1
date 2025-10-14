<#
.SYNOPSIS
  Calculates percentage of code contribution per author in the repository.
.DESCRIPTION
  Two modes:
    -Mode current : Uses `git blame` over the current working tree to count the lines *present now* attributed to each author.
    -Mode added   : Uses `git log --numstat` to sum lines *added historically* by each author.

  You can include or exclude file extensions / paths.

.EXAMPLE
  powershell -ExecutionPolicy Bypass -File scripts\contrib-percentage.ps1
  (Current snapshot percentages for common source files.)

.EXAMPLE
  powershell -File scripts\contrib-percentage.ps1 -Mode added -Json > contrib.json

.NOTES
  - Requires git in PATH.
  - For performance, extremely large repos may take time in current mode (git blame per file).
  - Customize $AuthorAliases to merge duplicate author identities.
#>
param(
  [ValidateSet('current','added')] [string]$Mode = 'current',
  [string[]]$IncludeExtensions = @('java','kt','groovy','xml','yml','yaml','properties','sql','html','css','js','ts'),
  [string[]]$ExcludePaths = @('target/','build/','dist/','out/','node_modules/','.idea/','.git/'),
  [switch]$IncludeTests,
  [switch]$ShowFiles,
  [switch]$Json,
  [switch]$VerboseTiming
)

# Map alternate author spellings / emails to a canonical name if needed.
$AuthorAliases = @{
  # 'Old Name' = 'Canonical Name'
}

function Write-VerboseMsg($msg) { if ($VerboseTiming) { Write-Host "[INFO] $msg" -ForegroundColor Cyan } }

function Get-CodeFiles {
  Write-VerboseMsg "Collecting tracked files"
  $files = git ls-files | Where-Object { $_ -ne '' }
  $filtered = @()
  foreach ($f in $files) {
    $skip = $false
    foreach ($ex in $ExcludePaths) { if ($f -like "$ex*") { $skip = $true; break } }
    if ($skip) { continue }
    if (-not $IncludeTests -and ($f -match '(?i)\\test\\' -or $f -match '(?i)/test/')) { continue }
    $ext = ($f.Split('.') | Select-Object -Last 1)
    if ($IncludeExtensions -contains $ext) { $filtered += $f }
  }
  if ($ShowFiles) {
    Write-Host "Files considered ($($filtered.Count)):" -ForegroundColor Yellow
    $filtered | ForEach-Object { Write-Host "  $_" }
  }
  return $filtered
}

function Normalize-Author($name) {
  if ($AuthorAliases.ContainsKey($name)) { return $AuthorAliases[$name] }
  return $name
}

function Mode-Current {
  $files = Get-CodeFiles
  if ($files.Count -eq 0) { Write-Warning "No files matched filters."; return @{} }
  $counts = @{}
  $i = 0
  foreach ($file in $files) {
    $i++
    Write-VerboseMsg "Blaming ($i/$($files.Count)) $file"
    # Use porcelain blame for reliable parsing.
    git blame --line-porcelain -- "$file" 2>$null | Where-Object { $_ -like 'author *' } | ForEach-Object {
      $author = ($_ -replace '^author\s+','')
      if ($author -eq 'not committed yet') { $author = 'UNCOMMITTED' }
      $author = Normalize-Author $author
      if (-not $counts.ContainsKey($author)) { $counts[$author] = 0 }
      $counts[$author]++
    }
  }
  return $counts
}

function Mode-Added {
  Write-VerboseMsg "Parsing git log numstat"
  $counts = @{}
  $currentAuthor = $null
  git log --numstat --format='--%an <%ae>' | ForEach-Object {
    $line = $_
    if ($line -like '--*') {
      $authorRaw = $line.Substring(2)
      $currentAuthor = Normalize-Author $authorRaw
      if (-not $counts.ContainsKey($currentAuthor)) { $counts[$currentAuthor] = 0 }
    } elseif ($line -match '^(\d+|-)\t(\d+|-)\t(.+)$') {
      if (-not $currentAuthor) { return }
      $added = $Matches[1]
      $path = $Matches[3]
      if ($added -eq '-') { return } # binary
      # Filter similar to file filters
      foreach ($ex in $ExcludePaths) { if ($path -like "$ex*") { return } }
      if (-not $IncludeTests -and ($path -match '(?i)\\test\\' -or $path -match '(?i)/test/')) { return }
      $ext = ($path.Split('.') | Select-Object -Last 1)
      if ($IncludeExtensions -notcontains $ext) { return }
      $counts[$currentAuthor] += [int]$added
    }
  }
  return $counts
}

if (-not (Get-Command git -ErrorAction SilentlyContinue)) { Write-Error "git not found in PATH"; exit 1 }

$counts = if ($Mode -eq 'current') { Mode-Current } else { Mode-Added }
if ($counts.Keys.Count -eq 0) { Write-Warning "No contribution data collected."; exit 0 }

$total = ($counts.Values | Measure-Object -Sum).Sum

$result = $counts.GetEnumerator() | Sort-Object Value -Descending | ForEach-Object {
  [PSCustomObject]@{
    Author     = $_.Key
    Lines      = $_.Value
    Percentage = [Math]::Round(100.0 * $_.Value / $total, 2)
  }
}

if ($Json) {
  $result | ConvertTo-Json -Depth 4
} else {
  Write-Host "Mode: $Mode" -ForegroundColor Green
  Write-Host "Total lines considered: $total" -ForegroundColor Green
  $result | Format-Table -AutoSize
}

