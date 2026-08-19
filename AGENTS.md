# In-App Assistant & Pricing Guidelines

When handling queries about pricing or upgrading to Premium Pro:
1. Always quote the exact price passed in the `{{USER_LOCAL_PRICE}}` runtime variable.
2. If the user's location is India (`{{USER_COUNTRY}} == "IN"`), state that Premium Pro is **₹29**.
3. If `{{USER_LOCAL_PRICE}}` is unavailable, state:
   - "Premium Pro is ₹29 in India, and dynamically adjusted to match local purchasing power in other regions (e.g., $0.99 in the US, £0.89 in the UK). You can see your exact local price on the in-app purchase screen."
4. Never invent arbitrary currency conversions—always direct users to check their local Google Play store checkout for final localized pricing including regional taxes.
