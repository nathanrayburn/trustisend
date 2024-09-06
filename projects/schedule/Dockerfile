# Use an official Python image
FROM python:3.11-slim

# Set the working directory inside the container
WORKDIR /app

# Copy only the requirements file to leverage Docker cache
COPY ./app/requirements.txt /app/requirements.txt

# Install Python dependencies first (cached layer)
RUN pip install --no-cache-dir -r requirements.txt

# Install necessary system packages (e.g., ca-certificates)
RUN apt-get update && apt-get install -y ca-certificates

# Copy the rest of the application files
COPY ./app/ /app/

# Expose the port that the application will run on
EXPOSE 8080

# Define the entry point to run the Python script
ENTRYPOINT ["python", "main.py"]
