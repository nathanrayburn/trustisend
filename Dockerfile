# Use an official Python image
FROM python:3.11-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the Python script and other necessary files
COPY ./app/ /app/

# Install Python dependencies
COPY ./app/requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

EXPOSE 5000 5000
# Define the entry point to run the Python script
ENTRYPOINT ["python", "main.py"]