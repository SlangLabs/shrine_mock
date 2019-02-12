# shrine_mock
Sample e-commerce app with Slang integration

All the features mentioned below can be accessed by navigating through the app's UI as well.

This app has the following features
* Track orders
  - Can be triggered by the user saying things like "Track my orders". The user can also specify the brand, color and product
  that's required, by saying "Track my t-shirt order", "Where are my Adidas shoes", "Track my white socks".
  - Can also be triggered by user saying "Track my last order" which would show the most recent order the user placed.
  
* Track refunds
  - Can be triggered by the user saying things like "Track my refunds". The user can also specify the brand, color and product
  that's required, by saying "Track my t-shirt refund", "Show refund status for my Adidas shoes".
  - Can also be triggered by user saying "Track refund status" which would show the refund status for the most recent refund
  request.
  
* Return products
  - Can be triggered by the user saying things like "I want to return my jeans". The user can also specify the brand, color
  and product that's required.
  - Can also be triggered by user saying "I want to return my order" which would trigger return for the most recent order
  placed.

* Cancel orders
  - Can be triggered by the user saying things like "I want to cancel my jeans order". The user can also specify the brand,
  color and product that's needed.
  - Can also be triggered by user saying "I want to cancel my last order" which would trigger cancellation for the most recent 
  order placed.

* Contact customer support
  - Can be triggered by saying "I want to talk to customer support"

\
The code that sets the functionality for Slang is 
https://github.com/SlangLabs/shrine_mock/blob/new_mode/app/src/main/java/in/slanglabs/shrinemock/slang/VoiceInterface.java
