DELETE a1 FROM article_attachments a1
INNER JOIN article_attachments a2
WHERE a1.id > a2.id
  AND a1.article_id = a2.article_id
  AND a1.hash = a2.hash;
