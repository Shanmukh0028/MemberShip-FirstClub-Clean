You are tasked with designing the backend system for a Membership Program for a FirstClub. The platform aims to offer users subscription-based memberships with tiered benefits, and a smooth experience integrated with the shopping and checkout journey.

Key Requirements:

Membership Plans:
· Users can choose from Monthly, Quarterly, and Yearly membership plans.
· Each plan comes with specific pricing

Membership Benefits: {Optional}
· Free delivery on eligible orders.
· Extra X% discount on selected items or categories.
· Access to exclusive deals and early access to sales.
· Optional: Priority support for premium members.
· Each tier unlocks additional perks (e.g., higher discounts, faster delivery, exclusive coupons). - should be configurable

User Actions:
· Get Membership Plans and Tier to be selected by the user
· Subscribe to a plan (plan + tier).
· Upgrade, downgrade (Membership Tier), or cancel a subscription.
· Track current membership and expiry.

Membership Tiers:
· Users move through tiers (e.g., Silver, Gold, Platinum) based on criteria like:
○ Number of Order more than X
○ Total Order value in a month
○ User belonging to a certain cohort

The code should be running, demo-able and APIs should be functional. You will be evaluated on the abstractions created, entity design, extensibility and modularity. Follow the best practices for Java, bonus for thinking around concurrency.