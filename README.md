# FINT Java SSE Adapter Skeleton
[![FINT javadocs](https://img.shields.io/badge/FINT-javadocs-blue.svg)](https://docs.felleskomponent.no/fint-sse-adapter-skeleton/)

## Introduction
This project is a skeleton for develop FINT Java SSE adapter. You can find more information about adapter development in
the following links:

* [Overview](https://fintprosjektet.github.io/adapter/overview/)
* [Tutorial](https://fintprosjektet.github.io/adapter/tut-java-sse/)

## Packages and files
The adapter is divided into to main packages. The `adapter package` is the core adapter code. In general this don't need
any customization. The `customcode package` (which should be named for example after the application the adapter talks to)
is where the logic of the adapter is placed.

### EventHandlerService.java
The actions is handled in the `handleEvent()` method. The actions are defined as enums in the models:

```java
  public void handleEvent(Event event) {
   if (event.isHealthCheck()) {
       postHealthCheckResponse(event);
   } else {
       Event<FintResource> responseEvent = new Event<>(event);
       responseEvent.setStatus(Status.PROVIDER_ACCEPTED);
       eventStatusService.postStatus(responseEvent);

       /*
        * Add if statements for all the actions
        */

       responseEvent.setStatus(Status.PROVIDER_RESPONSE);
       eventResponseService.postResponse(responseEvent);
   }
}
```

## Adapter configuration
| Key | Description | Example |
|-----|-------------|---------|
| fint.adapter.organizations | List of orgIds the adapter handles. | rogfk.no, vaf.no, ofk.no |
| fint.adapter.sse-endpoint | Url to the sse endpoint. | https://play-with-fint-adapter.felleskomponent.no/provider/sse/%s |
| fint.adapter.status-endpoint | Url to the status endpoint. | https://play-with-fint-adapter.felleskomponent.no//provider/status |
| fint.adapter.response-endpoint | Url to the response endpoint. | https://play-with-fint-adapter.felleskomponent.no/provider/response |


- **[SSE Configuration](https://github.com/FINTlibs/fint-sse#sse-configuration)**
- **[OAuth Configuration](https://github.com/FINTlibs/fint-sse#oauth-configuration)** 
