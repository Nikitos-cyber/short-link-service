# Сервис коротких ссылок (short-link-service)

Веб-приложение на Spring MVC, которое превращает длинные адреса в короткие вида `https://click.ru/aB3dE9`. Регистрация не нужна: пользователь определяется по cookie, который сервис выдаёт при первом входе с помощью UUID. Удалённые ссылки не пропадают бесследно: сервис показывает уведомление и раскрывающийся список удалённых ссылок.

## Содержание

1. [Возможности](#возможности)
2. [Требования](#требования)
3. [Установка и запуск](#установка-и-запуск)
4. [Настройки](#настройки)
5. [Как пользоваться сервисом](#как-пользоваться-сервисом)
6. [Поддерживаемые команды](#поддерживаемые-команды)
7. [Примеры работы](#примеры-работы)
8. [Архитектура и принятые решения](#архитектура-и-принятые-решения)
9. [Тестирование](#тестирование)
10. [Частые проблемы](#частые-проблемы)

## Возможности

| Функция | Описание |
|---|---|
| Добавить | Создаёт короткую ссылку из длинного адреса |
| Удалить | Удаляет ссылку и переносит её в список удалённых |
| Обновить | Меняет настройки ссылки (лимит переходов) |
| Переадресация | Обрабатывает переход по короткой ссылке |
| UUID и Cookie | Определяют пользователя без регистрации |
| Запись в файл | Данные хранятся в файле `data.bin` |
| Уведомления | Сообщение об удалении и список удалённых ссылок |
| Копировать | Кнопка копирования короткой ссылки в буфер обмена |

## Требования

- JDK 25 или новее (минимальная версия для сборки проекта).
- Apache Tomcat 11.0.x (Jakarta EE 11, пакеты `jakarta.*`).
- Современный браузер (Chrome, Firefox, Edge, Safari).
- Для сборки: IntelliJ IDEA либо система сборки, настроенная в проекте.

## Установка и запуск

1. **Получите проект** и откройте его в IDE.
2. **Проверьте настройки** в `application.properties` (см. раздел «Настройки»).
3. **Соберите WAR-архив.** В IntelliJ IDEA: Build → Build Artifacts → `short-link-service:war`. Если проект собирается Maven или Gradle, используйте их стандартную команду сборки WAR.
4. **Разверните в Tomcat.** Приложение работает в корневом контексте:
   - остановите Tomcat;
   - удалите папку `webapps/ROOT` и всё, что в ней лежит;
   - скопируйте собранный WAR в `webapps` под именем `ROOT.war`.
5. **Запустите Tomcat:**
   - Windows: `bin\catalina.bat run`
   - Linux и macOS: `bin/catalina.sh run`
6. **Откройте** `http://localhost:8080/`. Главная страница откроется по адресу `/home`.

Остановка: `bin\catalina.bat stop` (Windows) или `bin/catalina.sh stop`.

Чтобы запускать приложение из IDEA, добавьте конфигурацию Tomcat Server → Local и укажите артефакт `short-link-service:war` на вкладке Deployment.

## Настройки

Файл `application.properties`:

```properties
url.company = https://click.ru
length.url = 6
path.database.key = user.home
file.name.database = data.bin
```

| Ключ | Назначение |
|---|---|
| `url.company` | Домен, с которого начинается каждая короткая ссылка |
| `length.url` | Длина случайного суффикса короткой ссылки |
| `path.database.key` | Имя системного свойства Java, в котором лежит папка для данных (`user.home` — домашняя папка пользователя, от имени которого запущен Tomcat) |
| `file.name.database` | Имя файла с данными |

Файл данных лежит по пути `<домашняя папка>/data.bin`. Чтобы сбросить все данные, остановите Tomcat и удалите этот файл.

Значения подставляются через `@Value`, а сам файл подключается через `@PropertySource` вместе с `PropertySourcesPlaceholderConfigurer`.

## Как пользоваться сервисом

### Создать короткую ссылку

1. Скопируйте длинный адрес вместе с `https://`.
2. Вставьте его в поле добавления ссылки.
3. При желании задайте лимит переходов числом или оставьте отметку «без ограничений».
4. Нажмите кнопку добавления. Новая ссылка появится в списке «Ваши ссылки».

### Скопировать ссылку

Нажмите «Копировать» в карточке ссылки. Кнопка на пару секунд покажет «Скопировано». После этого адрес можно вставить через Ctrl+V.

### Перейти по ссылке

Откройте короткий адрес в браузере или передайте его другим. Сервис находит запись по суффиксу и обрабатывает переход. Если у ссылки есть лимит, после его исчерпания она перестаёт работать.

### Изменить настройки

В карточке нужной ссылки нажмите кнопку изменения, задайте новое число переходов или поставьте «без ограничений» (поле числа при этом отключается) и сохраните. «Отмена» возвращает прежние значения.

### Удалить ссылку

Нажмите кнопку удаления в карточке. Над списком появится уведомление «Ссылка удалена» (или «Ссылки удалены»). Нажмите «Показать удалённые ссылки», чтобы раскрыть список удалённых ссылок, и ещё раз, чтобы свернуть его.

> Не очищайте cookie сайта и не меняйте браузер: по cookie `UUID_COOKIE` сервис узнаёт ваши ссылки. В режиме инкогнито при каждом запуске создаётся новый пользователь.

## Поддерживаемые команды

Все команды работают через браузер или любой HTTP-клиент. После каждого изменяющего запроса сервис отвечает перенаправлением на `/home` (шаблон Post/Redirect/Get).

| Метод и адрес | Параметры | Что делает |
|---|---|---|
| `GET /` | нет | Перенаправляет на `/home` |
| `GET /home` | нет | Открывает главную страницу, выдаёт cookie с UUID при первом входе, показывает ссылки, уведомление и список удалённых |
| `POST /add-link` | `url` — длинный адрес | Создаёт короткую ссылку |
| `POST /update-link` | поля формы изменения (как в `main-page.jsp`) | Обновляет настройки ссылки |
| `POST /delete-link` | `link` — идентификатор ссылки из формы удаления | Удаляет ссылку и переносит её в удалённые |
| `GET /{суффикс}` | нет | Переход по короткой ссылке (обработчик `/**`) |

Cookie `UUID_COOKIE` передаётся браузером автоматически. В `curl` её нужно сохранять и отправлять (см. ниже).

## Примеры работы

Адреса ниже приведены для локального запуска. `-c` и `-b` указывают файл, в котором curl хранит cookie.

**1. Получить cookie и открыть главную:**

```bash
curl -i -c cookies.txt -b cookies.txt http://localhost:8080/home
```

В ответе будет заголовок `Set-Cookie: UUID_COOKIE=...`, а в теле HTML-страница.

**2. Добавить ссылку:**

```bash
curl -i -c cookies.txt -b cookies.txt \
  -X POST --data-urlencode "url=https://example.com/catalog/shoes?color=black&size=42" \
  http://localhost:8080/add-link
```

Ожидаемый ответ: `302` и `Location: /home`. После этого на главной появится ссылка вида `https://click.ru/aB3dE9`.

**3. Удалить ссылку:**

```bash
curl -i -c cookies.txt -b cookies.txt \
  -X POST -d "link=<идентификатор из формы удаления>" \
  http://localhost:8080/delete-link
```

Ожидаемый ответ: `302` и `Location: /home`. На странице появится уведомление об удалении и запись в списке удалённых.

**4. Посмотреть результат:**

```bash
curl -s -b cookies.txt http://localhost:8080/home | grep -i "notice"
```

## Архитектура и принятые решения

Классическая трёхслойная схема на Spring MVC. Корневой пакет `ru.shortLinkService`.

```
Браузер ── HTTP ──► CommonController ──► LinkService ──► LinkRelationDao ─┐
              ▲            │                  │                           ├─► data.bin
              │            ▼                  ├──────► DeletedLinkDao ────┘
         main-page.jsp ◄── Model (LinkDto)    └──────► CompressedLink (суффикс)
```

| Слой | Класс | Ответственность |
|---|---|---|
| controller | `CommonController` | Маршруты, чтение cookie, выбор страницы или перенаправления |
| service | `LinkService` | Бизнес-логика: UUID, создание, изменение, удаление, переход |
| dao | `LinkRelationDao`, `DeletedLinkDao` | Чтение и запись активных и удалённых ссылок в файл |
| entity.dto | `LinkDto` | Данные для страницы: ссылки, их число, удалённые ссылки и их число |
| referenceConversion | `CompressedLink` | Генерация короткого суффикса заданной длины |
| view | `main-page.jsp`, CSS, JavaScript | Страница, уведомление, копирование |

Ключевые решения:

- **Пользователь определяется по cookie, который сервис выдаёт при первом входе с помощью UUID, а не по логину.** Так не нужны регистрация и база пользователей. Цена: потеря cookie означает потерю доступа к ссылкам.
- **Хранение в файле `data.bin`.** Подходит для небольшого приложения и не требует СУБД. Папка и имя файла вынесены в настройки.
- **Post/Redirect/Get.** После каждого изменения выполняется перенаправление на `/home`, поэтому обновление страницы не повторяет отправку формы.
- **Настройки в `application.properties`.** Домен, длина суффикса и путь к данным меняются без пересборки кода.
- **Внедрение зависимостей через конструктор.** Значения вроде длины суффикса передаются параметром конструктора с `@Value`, потому что поля с `@Value` заполняются уже после его вызова.
- **Удалённые ссылки хранятся отдельно** (`DeletedLinkDao`), поэтому их можно показать в уведомлении.
- **Страница на JSP и JSTL.** Условия и циклы написаны на `c:if`, `c:choose` и `c:forEach`. Для Tomcat 11 нужен URI `jakarta.tags.core`.
- **Копирование на JavaScript** с запасным способом (`execCommand`), если `navigator.clipboard` недоступен.

## Тестирование

### 1. Ручной сценарий (проверка основных функций)

| № | Шаги | Ожидаемый результат |
|---|---|---|
| 1 | Откройте `http://localhost:8080/` | Перенаправление на `/home`, в cookie появился `UUID_COOKIE` |
| 2 | Добавьте ссылку `https://example.com/test` | В списке появилась короткая ссылка, длина суффикса равна `length.url` |
| 3 | Нажмите «Копировать» и вставьте в адресную строку | Вставляется короткая ссылка, кнопка показала «Скопировано» |
| 4 | Откройте короткую ссылку | Сервис обработал переход |
| 5 | Измените лимит переходов, затем нажмите «Отмена» | Значения вернулись к прежним |
| 6 | Поставьте «без ограничений» | Поле числа отключилось |
| 7 | Удалите ссылку | Появилось уведомление, ссылка исчезла из списка |
| 8 | Раскройте «Показать удалённые ссылки» | В списке есть удалённая ссылка |
| 9 | Перезапустите Tomcat и откройте `/home` | Ссылки сохранились (данные взяты из `data.bin`) |
| 10 | Откройте сайт в режиме инкогнито | Список пуст (другой UUID) |
| 11 | Добавьте пустой адрес | Форма не отправляется или сервис возвращает понятную ошибку |

### 2. Проверка через curl

Команды из раздела «Примеры работы» служат дымовым тестом: каждая изменяющая команда должна вернуть `302` на `/home`, а `GET /home` с той же cookie показывать результат.

### 3. Автоматические тесты контроллера

Пример проверяет маршруты `CommonController` без запуска Tomcat. Он пока не входит в проект: добавьте его в `src/test/java`. Для запуска нужны зависимости `junit-jupiter`, `mockito-junit-jupiter` и `spring-test`, а также Servlet API в области `provided`.

```java
package ru.shortLinkService.controller;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.shortLinkService.entity.dto.LinkDto;
import ru.shortLinkService.service.LinkService;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(MockitoExtension.class)
class CommonControllerTest {

    @Mock LinkService service;
    @InjectMocks CommonController controller;

    MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void rootRedirectsToHome() throws Exception {
        mvc.perform(get("/"))
           .andExpect(status().is3xxRedirection())
           .andExpect(redirectedUrl("/home"));
    }

    @Test
    void homeShowsMainPageWithModel() throws Exception {
        when(service.generateUUID(any(), any())).thenReturn(UUID.randomUUID());
        when(service.findAllLinksUser(any())).thenReturn(mock(LinkDto.class));

        mvc.perform(get("/home"))
           .andExpect(status().isOk())
           .andExpect(view().name("main-page"))
           .andExpect(model().attributeExists("links", "linksDeleted",
                   "numberLinksUser", "numberLinksDeleted"));
    }

    @Test
    void addLinkSavesAndRedirects() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.getUUID(any())).thenReturn(id);

        mvc.perform(post("/add-link")
                   .cookie(new Cookie("UUID_COOKIE", id.toString()))
                   .param("url", "https://example.com"))
           .andExpect(redirectedUrl("/home"));

        verify(service).saveLink(id.toString(), "https://example.com");
    }

    @Test
    void deleteLinkCallsServiceAndRedirects() throws Exception {
        mvc.perform(post("/delete-link")
                   .cookie(new Cookie("UUID_COOKIE", "abc"))
                   .param("link", "aB3dE9"))
           .andExpect(redirectedUrl("/home"));

        verify(service).deleteLink(eq("abc"), eq("aB3dE9"));
    }
}
```

Если у `generateUUID` и `getUUID` другой возвращаемый тип, чем `UUID`, поправьте строки со `thenReturn`. Запуск: `mvn test` или `gradle test`, либо кнопка запуска у класса в IDEA.

## Частые проблемы

| Симптом | Причина и решение |
|---|---|
| Приложение не запускается, в логе `NullPointerException` в `LinkRelationDao.getPath()` | Не подставились значения из `application.properties`. Проверьте ключи, пробелы в конце строк и бин `PropertySourcesPlaceholderConfigurer` в корневом контексте |
| `Constructor threw exception` в `LinkService` | Длина суффикса передана полем с `@Value`. Передавайте её параметром конструктора |
| `JasperException` в JSP | Теги `c:` не подключены: нужен URI `jakarta.tags.core` и зависимость JSTL 3.0. В `c:forEach` пишите `items="${...}"`, а не просто имя |
| Кнопка «Копировать» не работает | Нет класса `js-copy` у кнопки или скрипт не загрузился. Откройте сайт по `localhost` или `https` |
| Не вижу свои ссылки | Очищена cookie, другой браузер или режим инкогнито |
| Русский текст на странице «кракозябрами» | В начале JSP нужна директива `contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"` |
