W EngineerApplication wykluczyłem SecurityAutoConfiguration.class ze spring boota bo kazał mi się logować kiedy chciałem coś testować

Swagger: http://localhost:8080/swagger-ui/index.html

//Z jakich elementów systemu może korzystać tylko admin?
- stworzenie nowego zespołu (endpoint: {/teams/create})
- stworzenie nowego projektu (endpoint: {/projects/create})
- dodanie użytkwonika do zespołu (endpoint: {api/...})
- dodanie zespołu do projektu (endpoint: {api/...})
- dodanie uzytkownika do projektu (endpoint: {api/...})
- dodanie nowego uzytkownika do systemu(???) (endpoint: {api/...})