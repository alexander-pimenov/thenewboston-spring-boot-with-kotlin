## Claude Code - полный гайд по AI-кодингу
Ссылка на видео: https://youtu.be/6NK4Pona2fY


### Установим Node.js
https://nodejs.org/en/download

### Установим anthropic-ai/claude-code для работы с claude-code:
```
npm install -g @anthropic-ai/claude-code
```


С ним можно работать из VS Code, установив расширение:
```
/rewind - для откатов изменений в клод-код
```

Создадим файлик для описания правил для Claude:
CLAUDE.md
```
- Never run "npm run dev".
- Use "npm run build" to ckeck if code compiles or not. See results and fix code if it's needed.
```

```
/init - генерирует CLAUDE.md для маркдаун гайд по проекту
```

### Установка инструментов для AI - MCP агенты:
```
claude mcp add blender uvx blender-mcp
```

1. Context7 - дает документацию библиотек
2. PostgreSQL MCP - для вайб-аналитики базы данных
3. TestSprite MCP - работает как аутсорс тестировщик
4. Supabase MCP - быстрая настройка БД

```
/compact - суммаризация текущего чата
```

### Gemini CLI - (про него https://youtu.be/6NK4Pona2fY?t=1674) - конкурент Claude
Установка через npm:
```
npm install -g @google/gemini-cli
```

---

Тренажер для подготовки к IT собеседованиям: https://solvit.space/l/stepoleg
Используйте промокод STEPOLEG для скидки 20% и неограниченного доступа к задачам

MCP-сервер для тестирования: http://testsprite.com

Купить подписку Claude Code за рубли: https://t.me/vc_pay_bot?start=6536573615

🔗 Как использовать PostgreSQL MCP: https://t.me/oleglimited/39
🔗 Конфиги MCP-серверов: https://oleg.stefanov.tech/mcp-collec...
🔗 Мой телеграм канал: https://t.me/oleglimited
🔗 Если нужно что-то автоматизировать/разработать/внедрить AI: https://t.me/oleg_code

---
