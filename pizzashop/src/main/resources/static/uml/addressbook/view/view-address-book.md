# View Address Book

Purpose
- The View Address Book screen lets a customer manage their delivery addresses. It shows the user’s saved addresses, supports adding a new address, editing or deleting an existing one, and marking one address as the default.

From this screen, the customer can:
- Review all saved delivery addresses.
- Add a new address (province/city, district, street, number, phone).
- Edit an existing address.
- Delete an address (soft delete in the system).
- Set or change the default address.
- See how many addresses are saved versus the maximum allowed.
- Navigate back to Profile or other sections via the sidebar.

Field list
| Field Name              | Description                                                                                 |
|-------------------------|---------------------------------------------------------------------------------------------|
| Full Name               | Read-only full name of the logged-in user (e.g., “Alice Customer”).                         |
| Province/City           | City or province of the address (required, up to 100 chars).                                |
| District                | District of the address (required, up to 100 chars).                                        |
| Street Name             | Street portion of the address (required, up to 255 chars).                                  |
| House Number            | Optional house or building number (up to 64 chars); stored as part of address line.         |
| Phone Number            | Optional contact phone for this address (up to 20 chars; digits, spaces, +, (), - allowed). |
| Set as default          | Checkbox to mark this address as the default for deliveries.                                |
| Add Button              | Submits the form to create the address (disabled if the user reached max addresses).        |
| Usage Counter Badge     | Shows “X/Y used” where X is the current number and Y is the max allowed.                    |
| Saved Address List      | Displays each saved address with full address text and phone.                               |
| Default Badge           | Indicates which address is currently set as default.                                        |
| Edit Link               | Opens the edit screen for the selected address.                                             |
| Delete Button           | Soft-deletes the selected address after confirmation.                                       |
| Alert (Error/Success)   | Inline messages showing validation errors or successful operations.                         |

Navigation
- Sidebar links to Profile, Address Book (current), Orders, Vouchers.
- Edit Address screen provides a Back link to return to the Address Book.

Notes
- Address uniqueness is not enforced; the same city/district/street can be reused.
- The first address created is automatically set as default if no other active addresses exist.
- Deleting the default address promotes the most recent remaining address as default (if any).

