# TMC Comet support #

A submodule of [TMC server](https://github.com/testmycode/tmc-server) providing HTTP push support using [cometd](http://cometd.org/).

## Protocol ##

Cometd's implementation of Bayeux is used as the transport. The following channels are defined.

- `/broadcast/tmc/global/admin-msg`
    - Publishes global messages from administrators, such as "maintenance window in 5 minutes".
    - Message payload: a string containing the message.
- `/broadcast/tmc/global/course-updated`
    - Published when a course is updated.
    - Message payload: a string containing the name of the course.
- `/broadcast/tmc/user/<username>/review-available`
    - Published when a review is available. The client should check the server for new reviews.
    - Message payload: `{exercise_name: '...', url: '<url to view review as HTML>'}`.

Clients authenticate by sending an ext parameter `authentication` containing the fields `username`, `password` and `serverBaseUrl` during handshake. Backends authorize by sending `backendKey` and `serverBaseUrl`. In both cases, `serverBaseUrl` should point to a TMC server instance. A fixed list of allowed server base URLs must be configured.

## Configuration and running ##

Install an init script (for debian-based systems) using `initscripts/install.sh [port]`. The port defaults to 8080.

To run the server manually, do `mvn -Dfi.helsinki.cs.tmc.comet.configFile=/path/to/file.properties jetty:start`. The configuration file should not be world-readable and must have the following definitions.

- `fi.helsinki.cs.tmc.comet.backendKey` a secret that all backend servers use to authenticate when publishing messages.
- `fi.helsinki.cs.tmc.comet.allowedServers` a semicolon-separated list of server base URLs that are accepted and authenticated against. Terminating slashes don't matter.
