# Add Delivery Address

Purpose
- Document the MVC structure and flow for adding a new delivery address to a user's address book.

Artifacts
- Class diagram: AddAddressClass.puml
- Sequence diagram: AddAddressSequence.puml

How to view
- Open the .puml files in an IDE with a PlantUML plugin/viewer, or paste their contents into an online PlantUML renderer.

Flow summary
1. Customer opens Address Book (GET /address).
2. System resolves currentUser, loads address list, and renders profile-address.html with the add form.
3. Customer submits the add form (POST /create-address).
4. Controller validates and delegates to AddressService.createAddress.
5. Service enforces max-per-user, optional default clearing, saves via AddressRepository.
6. Controller redirects back to /address with success/error flash messages.

Related files
- Controller: AddressController
- Service: AddressService, AddressServiceImpl
- Repository: AddressRepository
- Model: Address, User
- Form: AddressForm
- View: profile-address.html

