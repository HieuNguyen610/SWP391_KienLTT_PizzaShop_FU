# Edit Delivery Address

Purpose
- Allow the customer to update an existing delivery address and optionally set it as the default.

From this screen, the customer can:
- View the current address details.
- Modify Province/City, District, Street, Number, and Phone.
- Set/clear the default address flag.
- Save changes or delete the address.
- Navigate back to the Address Book.

Field list
| Field Name            | Description                                                                                                   |
|-----------------------|---------------------------------------------------------------------------------------------------------------|
| Full Name             | Read-only full name of the logged-in user (e.g., "Alice Customer").                                          |
| Province/City         | Required; up to 100 characters.                                                                               |
| District              | Required; up to 100 characters.                                                                               |
| Street Name           | Required; up to 255 characters. Combined with Number to form `addressLine`.                                   |
| House Number          | Optional; up to 64 characters. Combined into `addressLine` (format: `number, street`).                        |
| Phone Number          | Optional; up to 20 characters. Allowed: digits, spaces, plus (+), parentheses, hyphens; 8–15 digits required. |
| Set as default        | Checkbox to mark this address as the default. Clears previous default for the user when set.                  |
| Save Button           | Submits the form (POST /address/{id}/edit).                                                                  |
| Delete Button         | Soft-deletes the address (POST /address/{id}/delete) with confirmation.                                       |
| Back Link             | Returns to Address Book (/address).                                                                           |
| Alert (Error/Success) | Shows validation errors or confirmation messages.                                                             |

Validation summary
- Province/City: required, ≤100 chars
- District: required, ≤100 chars
- Street: required, ≤255 chars
- Number: optional, ≤64 chars
- Phone: optional, ≤20 chars, regex `^[0-9+()\-\s]*$`, and 8–15 digits after removing non-digits

Notes
- Ownership enforced: address must belong to the current user (`findByIdAndUser`).
- If the edited address is set as default, previous default is cleared.
- If a default address is later deleted, another address is promoted as default (if any).

