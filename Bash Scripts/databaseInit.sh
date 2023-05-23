#!/bin/bash

# Database connection details
DB_HOST="34.78.99.140"
DB_PORT="5432"
DB_NAME="postgres"
DB_USER="postgres"
DB_PASSWORD="root"

# Create a file called create_table.sql
SQL_SCRIPT="create_table.sql"

# Add table creation SQL code to the file
echo "CREATE TABLE public.advertisement
(
    id           bigserial PRIMARY KEY,
    description  varchar(255) NOT NULL,
    email        varchar(255) NOT NULL,
    image        varchar(255) NOT NULL,
    phone_number varchar(255) NOT NULL,
    title        varchar(255) NOT NULL
);

ALTER TABLE public.advertisement OWNER TO postgres;
" > $SQL_SCRIPT

# Check if the SQL script exists
if [ ! -f "$SQL_SCRIPT" ]; then
  echo "SQL script $SQL_SCRIPT not found."
  exit 1
fi

# Check if psql is installed
if ! [ -x "$(command -v psql)" ]; then
  echo "psql is not installed."
  exit 1
fi

# Check if the database exists
if ! psql "host=$DB_HOST port=$DB_PORT dbname=$DB_NAME user=$DB_USER password=$DB_PASSWORD" -c '\q' 2>/dev/null; then
  echo "Database $DB_NAME does not exist."
  exit 1
fi

# Check if the table already exists
if psql "host=$DB_HOST port=$DB_PORT dbname=$DB_NAME user=$DB_USER password=$DB_PASSWORD" -c '\d' | grep -q "advertisement"; then
  echo "Table already exists."
  exit 1
fi

# Create the table
psql "host=$DB_HOST port=$DB_PORT dbname=$DB_NAME user=$DB_USER password=$DB_PASSWORD" -f $SQL_SCRIPT

# Check if the table was created successfully
if [ $? -eq 0 ]; then
  echo "Table created successfully."
else
  echo "Table creation failed."
fi
