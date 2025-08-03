# Spotilyzer

This is initially a student project. This may be further developed in the future.
The code will be mainly in Grails, Vanilla JavaScript, and HTML/CSS. For future
interests, this can be refactored into react or vue.js.


## Development

### Running the application

To run the application, make sure you are on the right profile and port:
- For port tunneling, run and put in the password. If you need help, contact me.
```shell
  ssh -N -L 15432:localhost:5432 bdch@bdch -p 11111
```

- If you are on a Windows development machine, run
```
./gradlew bootRun "-Dgrails.env=development"
```
This sets the profile to development and will allow you to run the app
with the correct port.



## Grails 6.2.3 Documentation

- [org.bdch.User Guide](https://docs.grails.org/6.2.3/guide/index.html)
- [API Reference](https://docs.grails.org/6.2.3/api/index.html)
- [Grails Guides](https://guides.grails.org/index.html)
---

## Feature testcontainers documentation

- [https://www.testcontainers.org/](https://www.testcontainers.org/)

## Feature scaffolding documentation

- [Grails Scaffolding Plugin documentation](https://grails.github.io/scaffolding/latest/groovydoc/)

- [https://grails-fields-plugin.github.io/grails-fields/latest/guide/index.html](https://grails-fields-plugin.github.io/grails-fields/latest/guide/index.html)

## Feature spring-boot-devtools documentation

- [Grails SpringBoot Developer Tools documentation](https://docs.spring.io/spring-boot/docs/2.7.12/reference/htmlsingle/#using.devtools)

## Feature asset-pipeline-grails documentation

- [Grails Asset Pipeline Core documentation](https://www.asset-pipeline.com/manual/)

