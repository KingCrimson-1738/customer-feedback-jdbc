-- Oracle schema (12c or later). Run in SQL*Plus / SQL Developer / VS Code Oracle extension.
-- The Java app also creates this table automatically if it is missing.

CREATE TABLE feedback (
    id            NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    customer_name VARCHAR2(100) NOT NULL,
    service_name  VARCHAR2(100) NOT NULL,
    rating        NUMBER(1) NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comments      VARCHAR2(500),
    created_at    TIMESTAMP DEFAULT SYSTIMESTAMP
);

-- Sample data (optional)
INSERT INTO feedback (customer_name, service_name, rating, comments) VALUES ('Asha',  'Internet Broadband', 4, 'Fast and stable');
INSERT INTO feedback (customer_name, service_name, rating, comments) VALUES ('Ravi',  'Internet Broadband', 2, 'Frequent disconnections');
INSERT INTO feedback (customer_name, service_name, rating, comments) VALUES ('Meena', 'Mobile Recharge',    5, 'Instant recharge');
INSERT INTO feedback (customer_name, service_name, rating, comments) VALUES ('Karan', 'Mobile Recharge',    1, 'Payment failed twice');
INSERT INTO feedback (customer_name, service_name, rating, comments) VALUES ('Divya', 'DTH Service',        3, 'Average picture quality');
COMMIT;

-- The three queries the app runs:
-- Average rating per service
SELECT service_name, ROUND(AVG(rating), 2) AS avg_rating, COUNT(*) AS total
FROM feedback GROUP BY service_name ORDER BY avg_rating DESC;

-- Feedback rated below 3
SELECT id, customer_name, service_name, rating, comments
FROM feedback WHERE rating < 3 ORDER BY rating, id;
