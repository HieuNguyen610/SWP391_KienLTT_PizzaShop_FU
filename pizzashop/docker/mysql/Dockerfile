# Dockerfile for local MySQL used by the application
FROM mysql:8.0

# Default credentials and database (sensible defaults for local development)
# These can be overridden by docker-compose or environment when starting the container.
ENV MYSQL_ROOT_PASSWORD=rootpassword \
    MYSQL_DATABASE=mydb \
    MYSQL_USER=myuser \
    MYSQL_PASSWORD=mypass

# Optional: place any .sql or .sh init scripts in docker/mysql/initdb/ to be executed on container start
# COPY ./initdb/ /docker-entrypoint-initdb.d/

EXPOSE 3306
# The official image's ENTRYPOINT/CMD will run mysqld
