# PROMPTS.md

Registro de consultas a IA del laboratorio 11-12 (Fase 2, rama `mejora-ia`).

- **Proyecto:** Stockify · Productos (smartphones)
- **Integrantes:** Farid Chavez · Alonso Abad
- **Herramientas:** Gemini en Android Studio (gemini-3.6-flash, modo agente) para generar los cambios.
  Claude (Claude Code) para revisar el código generado antes de aceptarlo.

---

## Consulta 1 — Pruebas de favoritos al sincronizar
- **Fecha:** 08/11/2026
- **Integrante:** Farid Chavez
- **Herramienta:** Gemini en Android Studio (gemini-3.6-flash)
- **Prompt:** "En mi proyecto Android (Kotlin, MVVM con Room y Retrofit) tengo en data/mapper/Mappers.kt la función ItemEntity.fusionarCon(dto: ItemDto), que se usa al sincronizar para actualizar los datos de la API sobre la fila ya guardada en Room. Escribe pruebas unitarias con JUnit 4 que demuestren que: 1) el campo esFavorito se conserva al sincronizar, 2) se actualizan titulo, precio y descripcion desde la API y no se pierde categoriaId, 3) si editadoLocalmente es true, la entidad no cambia, 4) ItemDto.aEntity con brand null deja marca vacía y esFavorito en false, 5) ItemEntity.aModelo mantiene esFavorito. El precio se convierte con la función Double.dolaresASoles()."
- **Qué respondió (resumen):** Creó `MappersTest.kt` en `app/src/test` con 5 pruebas, una por cada punto pedido.
- **Qué usamos / qué cambiamos y por qué:** Revisamos que usara los nombres reales de `ItemEntity` y la firma `aEntity(categoriaId)`. Lo usamos sin cambios. La quinta prueba cubre también el caso no favorito, que no habíamos pedido. Resultado: 5 pruebas en verde.
- **Qué aprendimos:** Que `fusionarCon` conserva el favorito porque hace `copy()` de la fila guardada y solo reemplaza los campos que vienen de la API. Una prueba unitaria lo demuestra sin abrir la app.

## Consulta 2 — Manejar respuestas inválidas de la API
- **Fecha:** 08/11/2026
- **Integrante:** Farid Chavez
- **Herramienta:** Gemini en Android Studio (gemini-3.6-flash)
- **Prompt:** "En app/src/main/java/com/tecsup/productos/data/repository/ItemRepositoryImpl.kt, las funciones refrescar() y obtenerDetalle() solo atrapan IOException y HttpException. Si la API devuelve un JSON que no coincide con ItemDto, se lanza kotlinx.serialization.SerializationException y la app se cierra. Agrega el manejo de SerializationException en las dos funciones sin cambiar el resto de la lógica: en refrescar() debe devolver Result.failure(e), y en obtenerDetalle() debe devolver el producto guardado en Room si existe, igual que con IOException. No modifiques ningún otro archivo."
- **Qué respondió (resumen):** Agregó el import y un `catch (e: SerializationException)` en cada función.
- **Qué usamos / qué cambiamos y por qué:** Verificamos que solo cambiara `ItemRepositoryImpl.kt` y no tocara la lógica de `fusionarCon` ni de categorías. Lo usamos sin cambios. Corrimos las 43 pruebas del proyecto y siguieron en verde.
- **Qué aprendimos:** Que manejar solo "sin internet" y "error del servidor" no basta: si la API cambia su formato, la app se cerraba. Ahora muestra un mensaje o los datos guardados.

## Consulta 3 — Pruebas de errores del ListaViewModel
- **Fecha:** 08/11/2026
- **Integrante:** Farid Chavez
- **Herramienta:** Gemini en Android Studio (gemini-3.6-flash)
- **Prompt:** "En app/src/test/java/com/tecsup/productos/ui/lista/ ya existen Falsos.kt (ItemRepositoryFalso, NotificadorFalso, ProveedorTextosFalso, etc.) y ListaViewModelTest.kt, que usa la regla ReglaDespachadorPrincipal y crea el ListaViewModel con esos falsos. Crea un archivo NUEVO llamado ListaViewModelErroresTest.kt en el mismo paquete, reutilizando los mismos falsos y la misma forma de crear el ViewModel, con 4 pruebas JUnit 4: 1) si refrescar() falla con HttpException 500 y no hay datos guardados, el estado es UiState.Error con el texto de R.string.error_servidor, 2) si falla con kotlinx.serialization.SerializationException, el estado es UiState.Error con el texto de R.string.error_inesperado, 3) si falla con IOException, NO se envía ninguna notificación, 4) si la API responde una lista vacía, el estado es UiState.Exito con lista vacía y el aviso es null. ProveedorTextosFalso devuelve "texto-<id del recurso>". No modifiques ningún archivo existente."
- **Qué respondió (resumen):** Creó `ListaViewModelErroresTest.kt` con 4 pruebas, reutilizando los falsos y la regla del despachador.
- **Qué usamos / qué cambiamos y por qué:** Revisamos que no modificara `Falsos.kt` ni `ListaViewModelTest.kt`. Lo usamos sin cambios. La tercera prueba incluye un `assertNotNull(viewModel)` que no aporta, pero no afecta. Resultado: 4 pruebas en verde (47 en total).
- **Qué aprendimos:** Que con un repositorio falso se pueden simular errores (500, JSON inválido, sin internet) que con la API real no se pueden forzar a voluntad.

## Consulta 4 — Tiempos de espera en las peticiones
- **Fecha:** 08/11/2026
- **Integrante:** Farid Chavez
- **Herramienta:** Gemini en Android Studio (gemini-3.6-flash)
- **Prompt:** "En app/src/main/java/com/tecsup/productos/data/remote/RetrofitClient.kt, el OkHttpClient no tiene tiempos de espera configurados. Agrega connectTimeout de 15 segundos, readTimeout de 20 segundos y writeTimeout de 20 segundos usando java.util.concurrent.TimeUnit, y declara esos valores como constantes privadas dentro del object RetrofitClient. No cambies la BASE_URL, el Json ni el interceptor de logging. No modifiques ningún otro archivo."
- **Qué respondió (resumen):** Agregó el import de `TimeUnit`, tres constantes privadas y los tres tiempos en el `OkHttpClient.Builder`.
- **Qué usamos / qué cambiamos y por qué:** Lo usamos sin cambios. Gemini nombró las constantes en inglés (`CONNECT_TIMEOUT_SECONDS`), a diferencia del resto del proyecto, que está en español.
- **Qué aprendimos:** Que si se pasa el tiempo, OkHttp lanza `SocketTimeoutException`, que hereda de `IOException`. Por eso la app lo trata igual que "sin conexión" sin escribir código extra.

## Consulta 5 — Deslizar para actualizar la lista
- **Fecha:** 08/11/2026
- **Integrante:** Farid Chavez
- **Herramienta:** Gemini en Android Studio (gemini-3.6-flash)
- **Prompt:** "Quiero agregar "deslizar para actualizar" (pull to refresh) al catálogo usando PullToRefreshBox de Material 3. Haz estos cambios: 1) En ui/lista/ListaViewModel.kt: agrega un MutableStateFlow<Boolean> privado _refrescando y expón val refrescando: StateFlow<Boolean>. En la función refrescar(), al inicio pon _refrescando.value = true solo si _estadoBase ya es UiState.Exito (para que la primera carga siga mostrando el esqueleto), y al final, pase lo que pase, pon _refrescando.value = false. No cambies el resto de la lógica de refrescar(). 2) En ui/lista/ListaScreen.kt: agrega el parámetro refrescando: Boolean = false a ListaScreen y envuelve el Box(modifier = Modifier.weight(1f)) que contiene el AnimatedContent con PullToRefreshBox(isRefreshing = refrescando, onRefresh = acciones.onReintentar, modifier = Modifier.weight(1f)). 3) En ui/navigation/AppNav.kt: donde se crea ListaScreen, recoge viewModel.refrescando con collectAsStateWithLifecycle() y pásalo como refrescando. No modifiques ningún otro archivo ni la lógica existente."
- **Qué respondió (resumen):** Modificó los 3 archivos. En el ViewModel usó `try/finally` para que el indicador siempre se apague.
- **Qué usamos / qué cambiamos y por qué:** Revisamos que `refrescar()` conservara la notificación y el manejo sin conexión. En `ListaScreen`, Gemini reemplazó el `Box` por `PullToRefreshBox` en vez de envolverlo, lo cual es equivalente porque `PullToRefreshBox` ya es un `Box`. Como toca archivos de Alonso, lo coordinamos antes. Probado en el emulador: el círculo aparece, desaparece y llega la notificación.
- **Qué aprendimos:** Que el estado `refrescando` va en el ViewModel y la pantalla solo lo observa, como piden las reglas MVVM. Y que el gesto solo funciona con la lista arriba del todo.

---

## Reflexión final

Gemini nos ahorró tiempo sobre todo en el código repetitivo: generó en segundos las pruebas unitarias
(MappersTest y ListaViewModelErroresTest) y los cambios pequeños, como los tiempos de espera o los
`catch` de SerializationException. Aun así, no aceptamos ningún cambio sin leerlo: antes de pulsar
"Keep All" revisábamos cuántos archivos había modificado, porque en modo agente puede tocar archivos
que no le pedimos, incluidos los del compañero. En esa revisión encontramos detalles que corregir o
discutir: nombres de constantes en inglés en un proyecto en español, un `assertNotNull` que no
comprobaba nada, y un `Box` que reemplazó en lugar de envolverlo (era equivalente, pero tuvimos que
entender por qué). También aprendimos que un prompt preciso, que indica qué archivo cambiar y qué
NO tocar, da mejores resultados. La próxima vez acordaríamos desde el inicio que todas las mejoras
van en la rama mejora-ia, para no mezclarlas con main.