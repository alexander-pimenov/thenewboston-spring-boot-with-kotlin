
HIBERNATE_START

https://t.me/JavaLearnBot?domain=JavaLearnBot&start=c1724626210163-190-ds

https://stepik.org/lesson/2097859/step/1?unit=2128677

```sql
INSERT INTO cv_queue(
    "fileId",
    "fileName",
    "mimeType",
    "modifiedTime",
    "groupId",
    "folderName",
    "webViewLink",
    "platform",
    "rgGroup"
)
VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9)
ON CONFLICT ("fileId") DO UPDATE SET
    "modifiedTime" = EXCLUDED."modifiedTime",
    "folderName" = EXCLUDED."folderName";
```

"id" - Long
"fileId" - 1i-0XZMzn0GnA6xRcbQc6xbkjtisXpwT
"fileName" - Asim Shreberten Resume.pdf
"mimeType" - application/pdf
"modifiedTime" - Instant
"groupId" - UUID
"folderName" - Group 2
"webViewLink" - https://drive.google.com/file/d/1i-0XZMzn0GnA6xRcbQc6xbkjtisXpwT
"platform" - clickup / monday
"status" - completed
"attempts" - 1 (кличество попыток за какое резюме было загружено в программу clickup, предусмотренн retry)
"errorMessage" - null
"createdAt" - Instant
"startedAt" - Instant
"completedAt" - Instant
"clickupTaskId" - 86ew03b4v (id в clickup)
"consumerId" - int in [1,2,3,4], консюмер который прочитал и спарсил это резюме
"rgGroup" - UUID (мета информация для clickup)

N8N - tameka-nonremovable-heaven.ngrok-free.dev/workflow/NIRqEMIydPC0d1O3/executions/444429

cleanup workflow - очищает строки из БД, которые уже обработаны и записаны в clickup (чтобы таблица со временем не переполнялась):
```sql
DELETE FROM cv_queue
WHERE status = 'completed';
```



Используем фичу от Postgres "FOR UPDATE SKIP LOCKED" - лочим, чтобы несколько консюмеров не обрабатывали одни и теже строки в таблице:
```sql
WITH locked AS (
    SELECT
    id,
    "fileId",
    "fileName",
    "mimeType",
    "groupId",
    "folderName",
    "webViewLink",
    "platform",
    "modifiedTime"
    FROM cv_queue
    WHERE status = 'pending'
    ORDER BY "modifiedTime"
    LIMIT 60
    FOR UPDATE SKIPED LOCKED
)
UPDATE cv_queue
SET
    "status" = 'processing',
    "startedAt" = NOW(),
    "attempts" = attempts + 1,
    "consumerId" = 1
FROM locked
WHERE cv_queue.id = locked.id
RETURNING cv_queue.*;
```

Вставляем статус 'completed':
```sql
UPDATE cv_queue
SET
    "status" = 'completed',
    "completedAt" = NOW(),
    "clickupTaskId" = $2
WHERE "fileId" = $1;
```


