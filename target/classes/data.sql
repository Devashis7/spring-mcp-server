-- =============================================================================
-- data.sql  –  Seed data for the H2 in-memory Employee database
-- =============================================================================
-- This script is executed by Spring Boot after Hibernate creates the schema.
-- Columns: (name, department, salary, joining_date, status, last_active_date)
--
-- status         : ACTIVE | INACTIVE
-- last_active_date: the most recent date the employee was active; used for
--                   time-scoped queries such as "active users this month".
--
-- Current server date used in demo: 2026-02-25 (February 2026)
-- Several employees have last_active_date in 2026-02 so the
-- "getActiveEmployeesThisMonth" tool returns interesting results.
-- =============================================================================

-- IT Department
INSERT INTO employees (name, department, salary, joining_date, status, last_active_date) VALUES
  ('Alice Johnson',  'IT', 95000.00, '2019-03-15', 'ACTIVE',   '2026-02-20');
INSERT INTO employees (name, department, salary, joining_date, status, last_active_date) VALUES
  ('Bob Williams',   'IT', 78000.00, '2020-07-01', 'ACTIVE',   '2026-02-18');
INSERT INTO employees (name, department, salary, joining_date, status, last_active_date) VALUES
  ('Carol Martinez', 'IT',112000.00, '2018-01-10', 'INACTIVE', '2025-11-30');
INSERT INTO employees (name, department, salary, joining_date, status, last_active_date) VALUES
  ('David Lee',      'IT', 45000.00, '2023-06-20', 'ACTIVE',   '2026-02-24');
INSERT INTO employees (name, department, salary, joining_date, status, last_active_date) VALUES
  ('Eva Brown',      'IT', 63000.00, '2021-11-05', 'INACTIVE', '2026-01-15');

-- HR Department
INSERT INTO employees (name, department, salary, joining_date, status, last_active_date) VALUES
  ('Frank Wilson',   'HR', 55000.00, '2017-09-22', 'ACTIVE',   '2026-02-10');
INSERT INTO employees (name, department, salary, joining_date, status, last_active_date) VALUES
  ('Grace Taylor',   'HR', 72000.00, '2016-04-18', 'ACTIVE',   '2026-02-22');
INSERT INTO employees (name, department, salary, joining_date, status, last_active_date) VALUES
  ('Henry Anderson', 'HR', 48000.00, '2022-02-14', 'INACTIVE', '2025-12-01');

-- Finance Department
INSERT INTO employees (name, department, salary, joining_date, status, last_active_date) VALUES
  ('Isabella Thomas','Finance', 88000.00, '2015-12-03', 'ACTIVE',   '2026-02-19');
INSERT INTO employees (name, department, salary, joining_date, status, last_active_date) VALUES
  ('James Jackson',  'Finance',105000.00, '2014-08-30', 'ACTIVE',   '2026-02-25');
INSERT INTO employees (name, department, salary, joining_date, status, last_active_date) VALUES
  ('Karen White',    'Finance', 67000.00, '2020-05-11', 'INACTIVE', '2026-01-28');
INSERT INTO employees (name, department, salary, joining_date, status, last_active_date) VALUES
  ('Liam Harris',    'Finance', 42000.00, '2023-09-01', 'ACTIVE',   '2026-01-30');

-- Marketing Department
INSERT INTO employees (name, department, salary, joining_date, status, last_active_date) VALUES
  ('Mia Davis',      'Marketing', 59000.00, '2019-10-25', 'ACTIVE',   '2026-02-05');
INSERT INTO employees (name, department, salary, joining_date, status, last_active_date) VALUES
  ('Noah Garcia',    'Marketing', 74000.00, '2018-03-07', 'INACTIVE', '2025-10-14');
INSERT INTO employees (name, department, salary, joining_date, status, last_active_date) VALUES
  ('Olivia Miller',  'Marketing', 47000.00, '2022-07-19', 'ACTIVE',   '2026-02-23');

-- Operations Department
INSERT INTO employees (name, department, salary, joining_date, status, last_active_date) VALUES
  ('Paul Robinson',  'Operations', 53000.00, '2021-01-30', 'INACTIVE', '2025-09-20');
INSERT INTO employees (name, department, salary, joining_date, status, last_active_date) VALUES
  ('Quinn Clark',    'Operations', 82000.00, '2017-06-14', 'ACTIVE',   '2026-02-17');
INSERT INTO employees (name, department, salary, joining_date, status, last_active_date) VALUES
  ('Rachel Lewis',   'Operations', 61000.00, '2020-12-08', 'ACTIVE',   '2026-02-11');

-- Sales Department
INSERT INTO employees (name, department, salary, joining_date, status, last_active_date) VALUES
  ('Samuel Walker',  'Sales', 49000.00, '2023-03-15', 'INACTIVE', '2026-01-05');
INSERT INTO employees (name, department, salary, joining_date, status, last_active_date) VALUES
  ('Tina Hall',      'Sales', 93000.00, '2016-11-22', 'ACTIVE',   '2026-02-14');
