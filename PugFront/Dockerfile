FROM node:16

# Create and set the working directory
WORKDIR /app

# Copy package files and install dependencies
COPY package*.json ./
RUN npm install

# Copy the rest of the project files
COPY . .

# Copy the .env file
COPY .env .env

# Expose the application port
EXPOSE 3000

# Run the application
CMD ["node", "server.js"]