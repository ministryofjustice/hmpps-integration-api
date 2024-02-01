# 0009 - Authorisation at application level

2024-01-09

## Status

Accepted

## Context

As our project grows, we're dealing with a variety of users accessing our API. Managing who gets access to what is getting trickier. We need a way to control access that's both straightforward and flexible. 
Right now, there are lots of different endpoints and users, and without a centralized system, things can get messy. The goal is to keep things simple and organized while making sure everyone gets the right level of access. 
The decision is not just about handling our current challenges but also preparing for the future growth and changes in our project.

## Decision

To address the growing complexity of managing user access in our expanding project, we've decided to implement authorization directly within our application. 
This means setting up a system where each user's access is determined by their client certificate, making it easier to control who can access specific parts of our API.

### This decision allows for the following benefits

#### Simplified Approach
- User Focused Access: Authorization rules are tailored to individual clients based on their client certificates, ensuring a personalized and granular control over access permissions.
- Centralized Control: Managing authorization within the application simplifies access control. This centralization reduces the risk of inconsistencies and enhances overall system manageability.

#### Technical Benefits:
- Scalability: Centralized authorization is scalable, allowing our system to handle a growing number of clients and endpoints efficiently.
- Easy Integration: Integrating authorization into the application streamlines the development process, making it easier to maintain and update access rules.

#### Security Perspective:
- Client Certificate Security: Tying authorization to client certificates provides a robust authentication mechanism, enhancing overall security by ensuring that only authenticated and authorized users access our API.
- Reduced Surface Area: Centralized authorization reduces the attack surface by consolidating access control logic within the application, minimizing potential vulnerabilities.


## Consequences
- As the number of consumers and endpoints grows, maintaining the authorisation configuration may become complex. Regular reviews and documentation updates are essential to ensure accuracy and security.
- The success of this authorization model relies on the effective management of client certificates. Any compromise in certificate security can impact the integrity of the authorization framework.
- Changes to consumer permissions require updates to the application YAML files. This may introduce a slight delay in adjusting access control policies, especially in environments with strict change control processes.
- The centralized nature of the authorization configuration ensures consistency in access control policies across different environments. However, it requires diligent coordination to avoid misconfigurations during updates.