DELETE FROM transaction;
DELETE FROM region;
INSERT INTO region (Code, Description ) VALUES ('EAP', 'East Asia and Pacific' );
INSERT INTO region (Code, Description ) VALUES ('ECA', 'Europe and Central Asia');
INSERT INTO region (Code, Description ) VALUES ('HIC', 'High-Income countries');
INSERT INTO region (Code, Description ) VALUES ('LAC', 'Latin America and the Caribbean');
INSERT INTO region (Code, Description ) VALUES ('MENA', 'The Middle East and North Africa');
INSERT INTO region (Code, Description ) VALUES ('SA', 'South Asia');
INSERT INTO region (Code, Description ) VALUES ('SSA', 'Sub-Saharan Africa');

DELETE FROM feedback;
INSERT INTO feedback (validity, feedback, target, increase) VALUES ('ALLOWED', 'MANUAL_PROCESSING', 'ALLOWED', 0);
INSERT INTO feedback (validity, feedback, target, increase) VALUES ('ALLOWED', 'PROHIBITED', 'ALLOWED', 0);
INSERT INTO feedback (validity, feedback, target, increase) VALUES ('ALLOWED', 'PROHIBITED', 'MANUAL_PROCESSING', 0);
INSERT INTO feedback (validity, feedback, target, increase) VALUES ('MANUAL_PROCESSING', 'ALLOWED', 'ALLOWED', 1);
INSERT INTO feedback (validity, feedback, target, increase) VALUES ('MANUAL_PROCESSING', 'PROHIBITED', 'MANUAL_PROCESSING', 0);
INSERT INTO feedback (validity, feedback, target, increase) VALUES ('PROHIBITED', 'ALLOWED', 'ALLOWED', 1);
INSERT INTO feedback (validity, feedback, target, increase) VALUES ('PROHIBITED', 'ALLOWED', 'MANUAL_PROCESSING', 1);
INSERT INTO feedback (validity, feedback, target, increase) VALUES ('PROHIBITED', 'MANUAL_PROCESSING', 'MANUAL_PROCESSING', 1);

DELETE FROM amount;
INSERT INTO amount (max_amount, target, number) VALUES (200, 'ALLOWED', 'none');
INSERT INTO amount (max_amount, target, number) VALUES (1500, 'MANUAL_PROCESSING', 'none');