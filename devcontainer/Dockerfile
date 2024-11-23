# Use official TomEE base image
FROM tomee:10.1.251-jdk-21-openjdk

# Install Maven
RUN apt-get update && apt-get install -y maven

# Set working directory
WORKDIR /usr/src/app

# Copy project files to the container
COPY . /usr/src/app

# Build the project
RUN mvn clean package

# Deploy WAR file to TomEE
RUN cp target/Project4.war /usr/local/tomee/webapps/ROOT.war

# Expose port 8080
EXPOSE 8080

# Start TomEE
CMD ["catalina.sh", "run"]
