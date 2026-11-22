
# Node.js executa o Expo e instala as dependências.
FROM node:22-alpine

WORKDIR /app

# Instalar dependências travadas no package-lock.json.
COPY package.json package-lock.json ./
RUN npm ci

COPY . .

# Porta utilizada pelo Metro Bundler.
EXPOSE 8081

# Desativa a telemetria do Expo.
ENV EXPO_NO_TELEMETRY=1

# Inicia o servidor para Development Build Android.
CMD ["npx", "expo", "start", "--dev-client", "--host", "lan", "--port", "8081"]
