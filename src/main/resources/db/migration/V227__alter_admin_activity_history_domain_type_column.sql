UPDATE `koin`.`admins_activity_history`
SET domain_name = CASE domain_name
                      WHEN 'TECHSTACKS' THEN 'TECH_STACKS'
                      WHEN 'NOTICE' THEN 'NOTICES'
                      WHEN 'VERSION' THEN 'VERSIONS'
                      WHEN 'BENEFIT' THEN 'BENEFITS'
                      WHEN 'BENEFITCATEGORIES' THEN 'BENEFIT_CATEGORIES'
                      WHEN 'SHOPSCATEGORIES' THEN 'SHOPS_CATEGORIES'
                      WHEN 'MENUSCATEGORIES' THEN 'MENUS_CATEGORIES'
                      WHEN 'ABTEST' THEN 'ABTESTS'
                      WHEN 'COOPSHOP' THEN 'COOP_SHOPS'
                      WHEN 'STUDENT' THEN 'STUDENTS'
                      WHEN 'OWNER' THEN 'OWNERS'
                      WHEN 'ADMIN' THEN 'ADMINS'
                      WHEN 'CLUB' THEN 'CLUBS'
                      ELSE domain_name
    END
WHERE domain_name IN ('TECHSTACKS', 'NOTICE', 'VERSION', 'BENEFIT', 'BENEFITCATEGORIES',
                      'SHOPSCATEGORIES', 'MENUSCATEGORIES', 'ABTEST', 'COOPSHOP',
                      'STUDENT', 'OWNER', 'ADMIN', 'CLUB');
