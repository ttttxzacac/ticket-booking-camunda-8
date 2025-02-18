# Use the official Node.js image
FROM node:18

# Set the working directory
WORKDIR /usr/src/app

# Copy package.json and package-lock.json and install dependencies
COPY package*.json ./
RUN npm install

# Copy all source code
COPY . .

# Compile TypeScript code
RUN npm run build

# Run the application (RUN npm run build will compile src/app.ts into dist/app.js)
CMD ["node", "dist/app.js"] 
