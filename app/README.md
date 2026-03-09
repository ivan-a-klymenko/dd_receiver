# DD Receiver (app)

Краткие инструкции по сборке и тестированию локального HTTP‑сервера (NanoHTTPD) внутри Android
приложения.

Требования

- JDK (11 или 17) установлен и доступен в PATH (для macOS можно установить через Homebrew:
  `brew install openjdk@17`).
- Android SDK и adb
- Устройство или эмулятор Android (minSdk 31)

Быстрая сборка и установка

```bash
# на macOS: пример установки JDK 17
brew install openjdk@17
export JAVA_HOME="$(/usr/libexec/java_home -v17)"

# собрать приложение
./gradlew :app:assembleDebug
# установить на подключённое устройство / эмулятор
./gradlew :app:installDebug
```

Запуск и тестирование HTTP сервера в приложении

1. Откройте приложение, перейдите в вкладку "Server" и нажмите "Start". Появится foreground
   notification.
2. На хосте можно использовать `adb reverse` чтобы пробросить порт 8080 с устройства на хост:

```bash
adb reverse tcp:8080 tcp:8080
curl -v -X POST -H "Content-Type: application/json" -d '{"id":"m1","timestamp":1670000000000,"payload":{"k":"v"}}' http://localhost:8080/report
```

Если сервер слушает только 127.0.0.1 на устройстве, `adb reverse` делает его доступным как
`localhost:8080` на хосте.

Также можно вызвать сервис напрямую через adb:

```bash
# старт сервиса (эквивалент кнопки Start)
adb shell am start-foreground-service -n tech.airobotics.dd_receiver/.server.LocalHttpService -a tech.airobotics.dd_receiver.ACTION_START

# остановка
adb shell am start-foreground-service -n tech.airobotics.dd_receiver/.server.LocalHttpService -a tech.airobotics.dd_receiver.ACTION_STOP
```

Ожидаемое поведение

- POST /report -> 200 OK {"status":"ok"}
- UI обновляется и показывает полученные сообщения (in-memory)
- Foreground notification остаётся пока сервис работает

Безопасность и рекомендации

- По умолчанию сервер бинтится на 127.0.0.1 (локально). Если нужно открыть доступ в LAN — измените
  bindAddress на 0.0.0.0 (не рекомендуется без аутентификации).
- Рекомендация: добавить X-Auth-Token в заголовки и проверку на сервере.
- Контролируйте максимальный размер тела запроса (чтобы избежать OOM).

Дальнейшие улучшения (по желанию)

- Добавить Room для постоянного хранения сообщений
- Добавить простую auth (X-Auth-Token)
- Написать unit тесты для MVI reducer/store (Turbine)

Если хотите, я могу прямо сейчас добавить: 1) проверку X-Auth-Token и поле в UI, 2) Room
persistence, или 3) тесты для reducer/store — скажите, что приоритетнее.

