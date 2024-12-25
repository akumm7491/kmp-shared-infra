Feature: Service Registry
  As a microservice
  I want to register and discover services
  So that I can communicate with other services

  Background:
    Given the service registry is running
    And authentication is enabled
    And caching is enabled

  Scenario: Register a new service
    Given I have a valid service instance
      | name     | host          | port | status |
      | test-app | localhost     | 8080 | UP     |
    When I send a registration request
    Then the service should be registered successfully
    And a registration event should be published
    And metrics should be recorded
      | metric                  | value |
      | service.registrations   | 1     |
      | service.instances.total | 1     |

  Scenario: Update service status
    Given I have a registered service
      | name     | host      | port | status |
      | test-app | localhost | 8080 | UP     |
    When I update the service status to "DOWN"
    Then the service status should be updated successfully
    And a status update event should be published
    And metrics should be recorded
      | metric               | value |
      | service.status.down  | 1     |
      | service.status.total | 1     |

  Scenario: Deregister service
    Given I have a registered service
      | name     | host      | port | status |
      | test-app | localhost | 8080 | UP     |
    When I send a deregistration request
    Then the service should be deregistered successfully
    And a deregistration event should be published
    And metrics should be recorded
      | metric                  | value |
      | service.deregistrations | 1     |
      | service.instances.total | 0     |

  Scenario: Service registration with invalid data
    Given I have an invalid service instance
      | name | host | port  | status |
      |      |      | -1    | INVALID|
    When I send a registration request
    Then the registration should fail with validation errors
      | field    | error                    |
      | name     | must not be empty        |
      | host     | must not be empty        |
      | port     | must be greater than 0   |
      | status   | must be a valid status   |
    And no event should be published
    And error metrics should be recorded
      | metric                    | value |
      | validation.errors.total   | 1     |
      | validation.errors.name    | 1     |
      | validation.errors.host    | 1     |
      | validation.errors.port    | 1     |
      | validation.errors.status  | 1     |

  Scenario: Service discovery
    Given the following services are registered
      | name      | host      | port | status |
      | service-a | localhost | 8081 | UP     |
      | service-b | localhost | 8082 | UP     |
      | service-c | localhost | 8083 | DOWN   |
    When I request all services
    Then I should receive a list of 3 services
    And 2 services should be healthy
    And metrics should be recorded
      | metric                | value |
      | discovery.requests    | 1     |
      | discovery.time       | any   |

  Scenario: Cache behavior
    Given I have a registered service
      | name     | host      | port | status |
      | test-app | localhost | 8080 | UP     |
    When I request the service details 3 times
    Then the storage should be accessed only once
    And cache metrics should be recorded
      | metric          | value |
      | cache.hits      | 2     |
      | cache.misses    | 1     |
      | cache.hit.ratio | 0.67  |

  Scenario: Performance under load
    Given 100 services are registered
    When I send 1000 concurrent discovery requests
    Then all requests should complete within 5 seconds
    And no errors should occur
    And performance metrics should be recorded
      | metric                    | threshold |
      | request.time.avg          | 100ms     |
      | request.time.p95          | 200ms     |
      | system.cpu.usage         | 80%       |
      | system.memory.usage      | 80%       |

  Scenario: Error handling
    Given the storage service is unavailable
    When I send a registration request
    Then the request should fail gracefully
    And an error event should be published
    And error metrics should be recorded
      | metric                  | value |
      | errors.total           | 1     |
      | errors.storage         | 1     |
      | errors.registration    | 1     |
