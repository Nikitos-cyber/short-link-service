<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Сократитель ссылок</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
     <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
     <link href="https://fonts.googleapis.com/css2?family=Manrope:wght@400;600;800&family=JetBrains+Mono:wght@500&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="resources/css/main-page.css">
</head>
<body>
<main class="page">
    <h1>Сделайте ссылку короче</h1>
    <p class="lead">Вставьте длинный адрес — получите короткую ссылку, которой удобно делиться.</p>

    <form class="shorten" action="/add-link" method="post">
        <input
                type="url"
                name="url"
                placeholder="https://example.com/very/long/link"
                aria-label="Длинная ссылка"
                required>
        <button type="submit">Сократить</button>
    </form>
    <p class="hint">Ссылка должна начинаться с http:// или https://</p>

     <c:if test ="${not empty linksDeleted}">

              <ul class="notices" role="status" aria-live="polite">
                      <li class="notice">
                        <span class="notice__icon" aria-hidden="true">
                          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 6h18"/><path d="M8 6V4h8v2"/><path d="M19 6l-1 14H6L5 6"/><path d="M10 11v5M14 11v5"/></svg>
                        </span>
                       <p class="notice__title"> ${numberLinksDeleted > 1 ? 'Ссылки удалены' : 'Ссылка удалена' }</p>
                                         <p class="notice__text"> ${numberLinksDeleted > 1 ? 'Они больше не работают.'
                                          : 'Она больше не работает.' }</p>
                        <details class="notice__details">
                          <summary>Подробнее</summary>
                          <ul class="notice__links">
                           <c:forEach items="${linksDeleted}" var="linkDeleted">
                            <li>
                              <span class="notice__short">${linkDeleted.shortenedLink}</span>
                              <span class="notice__original">${fn:length(linkDeleted.fullLink.toString()) <= 30 ? linkDeleted.fullLink.toString() : fn:substring(linkDeleted.fullLink.toString(),0,41) += "..." }</span>
                            </li>
                            </c:forEach>
                        </details>
                      </li>
                    </ul>
      </c:if>

    <c:choose>
        <c:when test="${not empty links}">
    <section class="links" aria-labelledby="links-title">

        <div class="links-head">
            <h2 id="links-title">Ваши ссылки</h2>
            <span> ${numberLinksUser}  ${numberLinksUser % 10 == 0 || numberLinksUser % 10 > 4  ? 'ссылок' : numberLinksUser % 10 == 1 ? 'ссылка' : 'ссылки' }</span>
        </div>

        <ul class="links-list">
        <c:forEach items="${links}" var="link" varStatus="loop">

            <li class="link-item">
                      <a class="link-short" href="${link.suffixShortenLink}">${link.shortenedLink}</a>
                      <p class="link-original">${fn:length(link.fullLink.toString()) <= 30 ? link.fullLink.toString() : fn:substring(link.fullLink.toString(),0,41) += "..." }</p>
                      <span class="link-meta">${link.countFollowTheLinks} переходов · создана ${link.registrationTimeLink}</span>
                      <ul class="link-badges">
                      <c:choose>
                        <c:when test="${not empty link.timeToLeaveLink}">
                        <li class="badge ${ fn:contains(link.formatTime, 'мин') ? link.timeDiff < 15 ? 'badge--warn' : '' : '' }">Истекает через ${link.formatTime}</li>
                        </c:when>
                        <c:otherwise>
                         <li class="badge ">Без срока</li>
                        </c:otherwise>
                        </c:choose>
                        <c:choose>
                        <c:when test ="${link.limit >= 0}">
                        <li class="badge ${link.countFollowTheLinks / link.limit > 0.8 ? 'badge--warn' : '' }">Лимит: ${link.countFollowTheLinks} из ${link.limit}</li>
                        </c:when>
                          <c:otherwise>
                            <li class="badge">Без лимита переходов</li>
                          </c:otherwise>
                        </c:choose>
                      </ul>

                       <details class="edit">
                         <summary class="btn">Настроить ссылку</summary>
                          <form class="edit-form" action="/update-link" method="post">
                           <fieldset class="field">
                             <legend>Срок действия</legend>
                               <div class="segmented">
                                <input type="radio" id="ttl-${loop.count}-1h" name="ttl-${loop.count}" value="1h">
                                <label for="ttl-${loop.count}-1h">1 час</label>
                                <input type="radio" id="ttl-${loop.count}-24h" name="ttl-${loop.count}" value="24h">
                                <label for="ttl-${loop.count}-24h">24 часа</label>
                                <input type="radio" id="ttl-${loop.count}-1m" name="ttl-${loop.count}" value="1m" checked>
                                <label for="ttl-${loop.count}-1m">1 месяц</label>
                                <input type="radio" id="ttl-${loop.count}-none" name="ttl-${loop.count}" value="none">
                                <label for="ttl-${loop.count}-none">Без срока</label>
                                </div>
                                <p class="field-note">Отсчёт начнётся с момента сохранения. После окончания ссылка перестанет работать.</p>
                           </fieldset>

                           <div class="field">
                            <label for="limit-${loop.count}">Лимит переходов</label>
                            <div class="limit">
                            <input type="number" id="limit-${loop.count}" name="limit" min="1" step="1" inputmode="numeric" placeholder="100" value="10" required >
                            <label class="check">
                            <input type="checkbox" name="unlimited">
                            Без лимита
                            </label>
                            </div>
                             <p class="limit-error" role="alert">Введите целое число от 1 или отметьте «Без лимита».</p>
                             <p class="field-note">Когда лимит будет исчерпан, ссылка перестанет работать.</p>
                           </div>

                           <div class="edit-actions">
                           <input type="hidden" name="link" value="${link.shortenedLink}">
                           <button class="btn btn--primary" type="submit">Сохранить</button>
                           <button class="btn" type="reset">Отмена</button>
                           </div>
                          </form>
                         </details>
                      <div class="link-actions">
                        <button class="btn js-copy" type="button">Копировать</button>
                        <form action="/delete-link" method="post">
                        <input type="hidden" name="link" value="${link.shortenedLink}">
                        <button class="btn btn--danger" type="submit">Удалить</button>
                        </form>
                      </div>
                    </li>

            </c:forEach>

        </ul>
    </section>
    </c:when>
    <c:otherwise>
        <section class="links" aria-labelledby="empty-title">
              <div class="empty" role="status">
                <svg class="empty-icon" width="56" height="56" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                  <path d="M10 13a5 5 0 0 0 7.07 0l2.83-2.83a5 5 0 0 0-7.07-7.07L11.5 4.43"/>
                  <path d="M14 11a5 5 0 0 0-7.07 0L4.1 13.83a5 5 0 0 0 7.07 7.07l1.33-1.33"/>
                </svg>
                <h2 class="empty-title" id="empty-title">Ссылок пока нет</h2>
                <p class="empty-text">Здесь появятся ваши короткие ссылки. Чтобы добавить первую, вставьте длинный адрес в поле выше и нажмите «Сократить».</p>
              </div>
            </section>
    </c:otherwise>
    </c:choose>
</main>
 <script>

    document.querySelectorAll('.limit').forEach(function (box) {
      var num = box.querySelector('input[type="number"]');
      var chk = box.querySelector('input[type="checkbox"]');

      function sync() {
        num.disabled = chk.checked;
        num.required = !chk.checked;
      }

      chk.addEventListener('change', function () {
        sync();
        if (!chk.checked) num.focus();
      });

      // после «Отмена» состояние чекбокса возвращается, но change не срабатывает
      box.closest('form').addEventListener('reset', function () {
        setTimeout(sync, 0);
      });

      sync();
    });

   (function () {

    function getUrl(btn) {
      var direct = btn.getAttribute('data-copy');
      if (direct) return direct.trim();

      var item = btn.closest('.link-item') || btn.closest('li') || btn.parentElement;
      var link = item && item.querySelector('a.link-short');
      return link ? link.textContent.trim() : ''; // текст внутри <a>, а не href
    }

     function fallbackCopy(text) {
       var ta = document.createElement('textarea');
       ta.value = text;
       ta.setAttribute('readonly', '');
       ta.style.position = 'fixed';
       ta.style.top = '0';
       ta.style.opacity = '0';
       document.body.appendChild(ta);
       ta.focus();
       ta.select();
       ta.setSelectionRange(0, text.length);
       var ok = false;
       try { ok = document.execCommand('copy'); } catch (e) {}
       document.body.removeChild(ta);
       return ok;
     }

     function copyText(text) {
       if (navigator.clipboard && window.isSecureContext) {
         return navigator.clipboard.writeText(text).then(
           function () { return true; },
           function () { return fallbackCopy(text); }
         );
       }
       return Promise.resolve(fallbackCopy(text));
     }

     function flash(btn, ok) {
       if (!btn.hasAttribute('data-label')) btn.setAttribute('data-label', btn.textContent);
       clearTimeout(btn._timer);
       btn.textContent = ok ? 'Скопировано' : 'Не удалось';
       btn.classList.toggle('is-copied', ok);
       btn.classList.toggle('is-failed', !ok);
       btn._timer = setTimeout(function () {
         btn.textContent = btn.getAttribute('data-label');
         btn.classList.remove('is-copied', 'is-failed');
       }, 1800);
     }

     // Делегирование: работает и для кнопок, добавленных позже
     document.addEventListener('click', function (e) {
       var btn = e.target.closest ? e.target.closest('.js-copy') : null;
       if (!btn) return;
       e.preventDefault();

       var url = getUrl(btn);
       if (!url) {
         console.warn('Копирование: не найдена короткая ссылка рядом с кнопкой', btn);
         flash(btn, false);
         return;
       }
       copyText(url).then(function (ok) { flash(btn, ok); });
     });
   })();

  </script>
</body>
</html>
