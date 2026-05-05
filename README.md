# 📄 DocFlow API - Backend

> Solução robusta para orquestração de documentos, gestão de fluxos de aprovação e armazenamento seguro de ativos digitais.

---

![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen?style=flat-square&logo=springboot)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?style=flat-square&logo=postgresql)
![Docker](https://img.shields.io/badge/Docker-Enabled-blue?style=flat-square&logo=docker)
![License](https://img.shields.io/badge/license-MIT-blue?style=flat-square)

## 📋 Índice

- [Sobre o Projeto](#-sobre-o-projeto)
- [Funcionalidades Principais](#-funcionalidades-principais)
- [Tecnologias Utilizadas](#-tecnologias-utilizadas)
- [Arquitetura e Decisões Técnicas](#-arquitetura-e-decisões-técnicas)
- [Configuração do Ambiente](#-configuração-do-ambiente)
- [Como Executar](#-como-executar)
- [Documentação da API](#-documentação-da-api)
- [Testes](#-testes)
- [Padronização e Qualidade](#-padronização-e-qualidade)

---

## 📖 Sobre o Projeto

O **DocFlow** nasceu da necessidade de centralizar e automatizar o ciclo de vida de documentos corporativos. O sistema resolve o problema de fragmentação de arquivos e falta de rastreabilidade em processos de aprovação.

**Principais problemas resolvidos:**
- Centralização de documentos com versionamento.
- Controle de permissões granular (RBAC).
- Automação de workflows de aprovação (status flow).
- Auditoria completa de quem acessou ou modificou cada arquivo.

## ✨ Funcionalidades Principais

- **Gestão de Documentos:** Upload, versionamento e metadados customizáveis.
- **Workflow Engine:** Definição de estados (Rascunho, Em Revisão, Aprovado, Arquivado).
- **Segurança:** Autenticação JWT e integração com provedores OAuth2.
- **Busca Avançada:** Filtros por metadados e indexação de conteúdo.

## 🛠 Tecnologias Utilizadas

- **Linguagem:** Java 21 (LTS)
- **Framework Principal:** Spring Boot 3.x (Spring Security, Spring Data JPA)
- **Banco de Dados:** PostgreSQL (Persistência) e Redis (Caching de sessões)
- **Documentação:** Swagger / OpenAPI 3
- **Mensageria:** RabbitMQ (para processamento assíncrono de arquivos pesados)
- **Infra/DevOps:** Docker & Docker Compose

## 🏗 Arquitetura e Decisões Técnicas

O projeto segue os princípios da **Clean Architecture**, visando desacoplar a regra de negócio de frameworks externos:

- **Core Domain:** Contém as entidades de negócio e regras de workflow, sem dependências de infraestrutura.
- **Use Cases:** Orquestram o fluxo de dados.
- **Infrastructure:** Implementações de persistência, segurança e integrações externas.

**Por que Spring Boot?** Pela maturidade do ecossistema, facilidade de integração com Spring Security para conformidade com normas de segurança de dados e suporte nativo a GraalVM para redução de footprint em container.

## ⚙️ Configuração do Ambiente

### Pré-requisitos
- JDK 21
- Docker e Docker Compose
- Maven 3.9+

### Variáveis de Ambiente
Crie um arquivo `.env` na raiz seguindo o modelo:
```bash
cp .env.example .env
```
Explique as variáveis críticas:
- `DATABASE_URL`: String de conexão com o banco.
- `API_KEY`: Chave para integração com serviço X.

## 🚀 Como Executar

### Via Docker (Preferencial)
```bash
docker-compose up -d
```

### Localmente
```bash
# Instalar dependências
npm install # ou comando equivalente

# Rodar em modo desenvolvimento
npm run dev
```

## 🧪 Testes

A qualidade é inegociável. Descreva como rodar a suíte de testes.

```bash
# Testes unitários
npm run test

# Testes de integração
npm run test:integration

# Cobertura
npm run test:cov
```

## 🛡 Padronização e Qualidade

Para manter a consistência do código, utilizamos:
- **Linter:** ESLint/Prettier para formatação automática.
- **Conventional Commits:** Padronização de mensagens de commit.
- **Husky:** Git hooks para validar lint e testes antes do push.

## 🤝 Contribuição

1. Faça um Fork do projeto.
2. Crie uma Branch para sua Feature (`git checkout -b feature/nova-feature`).
3. Faça o Commit de suas mudanças (`git commit -m 'feat: add nova feature'`).
4. Faça o Push da Branch (`git push origin feature/nova-feature`).
5. Abra um Pull Request.

---

## 📄 Licença
Distribuído sob a licença MIT. Veja `LICENSE` para mais informações.