# Use an official Python image
FROM python:3.11-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the Python script and other necessary files
COPY ./app/ /app/

# Install Python dependencies
COPY ./app/requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Install necessary system packages
RUN apt-get update && apt-get install -y ntp ca-certificates

# Expose the port that the application will run on
EXPOSE 5000

# Define the entry point to run the Python script
ENTRYPOINT ["python", "main.py"]
