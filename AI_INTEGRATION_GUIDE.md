# 🤖 AI Integration Setup Guide

## Overview

This document provides comprehensive instructions for integrating AI services into your academic management system using both **GitHub Copilot PRO** and **ChatGPT PRO** capabilities.

## 🎯 What You Get

- **Dual AI Provider Support**: OpenAI (ChatGPT) + GitHub Models (Copilot PRO)
- **Intelligent Fallback**: Automatic failover between services
- **Academic Specialization**: AI prompts optimized for educational content
- **Conversation Context**: Maintains chat history for better responses
- **Easy Configuration**: Simple properties file setup

## 🚀 Quick Start

### Step 1: Configure API Keys

1. **Copy the template:**
   ```bash
   copy ai.properties.template ai.properties
   ```

2. **Get your OpenAI API key:**
   - Go to [OpenAI Platform](https://platform.openai.com/api-keys)
   - Log in with your ChatGPT PRO account
   - Create a new API key (starts with `sk-`)
   - Copy it to `ai.properties`

3. **Get your GitHub token:**
   - Go to [GitHub Settings > Tokens](https://github.com/settings/tokens)
   - Create a "Fine-grained personal access token"
   - Grant "Models" permission
   - Copy it to `ai.properties`

### Step 2: Configure Services

Edit `ai.properties`:

```properties
# Enable at least one service
ai.openai.enabled=true
ai.github.enabled=true

# Add your API keys
openai.api.key=sk-your-actual-openai-api-key-here
github.api.token=github_pat_your-actual-token-here
```

### Step 3: Test Configuration

Run the verification utility:
```bash
cd src/com/raven/config
java AIVerifier
```

### Step 4: Build and Run

```bash
ant clean compile run
```

## 📋 Required Dependencies

The following JAR files are needed in your `lib/` directory:

- ✅ `gson-2.10.1.jar` (automatically downloaded)
- ✅ Google OAuth libraries (already present)
- ✅ All existing project dependencies

## 🔧 Detailed Setup Instructions

### OpenAI API Setup (ChatGPT PRO)

1. **Create OpenAI Account**
   - Visit [platform.openai.com](https://platform.openai.com/)
   - Log in with your ChatGPT PRO credentials

2. **Generate API Key**
   - Navigate to "API Keys" section
   - Click "Create new secret key"
   - Copy the key (format: `sk-...`)
   - **Important**: Keep this key secure!

3. **Set Up Billing**
   - Add payment method in "Billing" section
   - Start with $20-50 credit
   - Set up usage alerts to monitor costs

4. **Configure in ai.properties**
   ```properties
   openai.api.key=sk-your-actual-key-here
   openai.model=gpt-4o
   ai.openai.enabled=true
   ```

### GitHub Models Setup (Copilot PRO)

1. **Prerequisites**
   - Active GitHub Copilot PRO subscription
   - Access to GitHub Models (currently in preview)

2. **Create Personal Access Token**
   - Go to [GitHub Settings > Developer settings > Personal access tokens](https://github.com/settings/tokens)
   - Click "Generate new token (fine-grained)"
   - Select appropriate repository scope
   - Grant "Models" permission
   - Copy the token (format: `github_pat_...`)

3. **Configure in ai.properties**
   ```properties
   github.api.token=github_pat_your-actual-token-here
   github.model=gpt-4o
   ai.github.enabled=true
   ```

## 🏗️ Architecture Overview

### Service Layer Structure

```
com.raven.service/
├── AIAssistantService.java      # Main coordinator service
├── OpenAIService.java           # ChatGPT PRO integration
├── GitHubModelsService.java     # Copilot PRO integration
└── ...

com.raven.config/
├── AIConfig.java                # Configuration manager
└── AIVerifier.java              # Setup verification

com.raven.model/
└── AIMessage.java               # Chat message model

com.raven.component/
└── PanelAIChat.java             # Chat UI component
```

### How It Works

1. **User Input**: Student types question in chat interface
2. **Service Selection**: System chooses best available AI provider
3. **Context Enhancement**: Adds academic-specific prompts
4. **API Call**: Sends request to OpenAI or GitHub Models
5. **Response Processing**: Formats and displays AI response
6. **Fallback**: Automatically tries alternative service if primary fails

## 💰 Cost Considerations

### OpenAI API Pricing
- **GPT-4o**: ~$0.005 per 1K input tokens, ~$0.015 per 1K output tokens
- **Average cost per message**: $0.01-0.03 (depending on length)
- **Monthly estimate**: $10-30 for moderate usage

### GitHub Models
- **Included with Copilot PRO subscription** ($20/month)
- **Usage limits**: Subject to GitHub's fair use policy
- **Best value for existing Copilot PRO users**

### Cost Optimization Tips
1. Start with GitHub Models if you have Copilot PRO
2. Use OpenAI as fallback or for specific use cases
3. Monitor usage through both platforms' dashboards
4. Set up billing alerts

## 🔒 Security Best Practices

### API Key Security
- ✅ Never commit `ai.properties` to version control
- ✅ Use environment variables in production
- ✅ Rotate keys regularly
- ✅ Monitor for unauthorized usage

### Configuration Files
```bash
# Add to .gitignore
ai.properties
*.key
*.secret
```

## 🧪 Testing Your Setup

### Manual Testing
1. Run `AIVerifier` to check configuration
2. Open the application and go to AI Chat
3. Send a test message: "Hello, can you help me with my studies?"
4. Verify you get a response from the AI

### Troubleshooting Common Issues

#### "AI services unavailable"
- Check `ai.properties` file exists and has correct keys
- Verify at least one service is enabled
- Run `AIVerifier` for detailed diagnostics

#### "OpenAI API error"
- Verify API key format (starts with `sk-`)
- Check billing is set up and has credit
- Ensure API key hasn't expired

#### "GitHub Models error"
- Verify token format (starts with `github_pat_`)
- Check Copilot PRO subscription is active
- Ensure Models permission is granted

#### Network/Firewall Issues
- Check internet connectivity
- Verify ports 443 (HTTPS) is not blocked
- Consider corporate firewall restrictions

## 🎓 Usage Examples

### Basic Academic Help
```
User: "How do I structure a research paper?"
AI: "A well-structured research paper typically follows this format:
1. Introduction with thesis statement
2. Literature review..."
```

### Project-Specific Guidance
```
User: "I'm working on a data structures project about binary trees"
AI: "For your binary tree project, I recommend:
- Start with basic node structure
- Implement insertion and search operations..."
```

### Methodology Questions
```
User: "What's the difference between qualitative and quantitative research?"
AI: "Great question! Here are the key differences:
Qualitative: Focuses on understanding concepts, experiences..."
```

## 📊 Monitoring and Analytics

### Usage Tracking
- OpenAI: Check usage at [platform.openai.com/usage](https://platform.openai.com/usage)
- GitHub: Monitor through GitHub Models dashboard
- Application: Check logs for response times and error rates

### Performance Metrics
- **Response Time**: Typically 1-3 seconds
- **Success Rate**: Should be >95% with fallback enabled
- **Token Usage**: Monitor to control costs

## 🔄 Updates and Maintenance

### Regular Tasks
- [ ] Check API key expiration dates
- [ ] Monitor usage and costs
- [ ] Update models when new versions available
- [ ] Review and update system prompts

### Version Updates
- Keep GSON library updated
- Monitor OpenAI/GitHub API changes
- Update model versions as needed

## 🆘 Support and Troubleshooting

### Getting Help
1. **Check Logs**: Look at console output for error details
2. **Run Diagnostics**: Use `AIVerifier` tool
3. **Test Individual Services**: Use built-in connection tests
4. **Check Status Pages**: 
   - [OpenAI Status](https://status.openai.com/)
   - [GitHub Status](https://www.githubstatus.com/)

### Common Error Messages

| Error | Cause | Solution |
|-------|-------|----------|
| "API key not configured" | Missing/invalid key | Check ai.properties file |
| "Service temporarily unavailable" | API service down | Try alternative provider |
| "Request timeout" | Network/latency issue | Check internet connection |
| "Rate limit exceeded" | Too many requests | Wait and retry |

## 🎉 Success Checklist

- [ ] ✅ API keys configured correctly
- [ ] ✅ At least one AI service working
- [ ] ✅ Chat interface responds to messages
- [ ] ✅ Academic prompts working well
- [ ] ✅ Fallback mechanism tested
- [ ] ✅ Usage monitoring set up
- [ ] ✅ Security measures in place

## 📚 Additional Resources

- [OpenAI API Documentation](https://platform.openai.com/docs)
- [GitHub Models Documentation](https://docs.github.com/en/github-models)
- [GitHub Copilot Documentation](https://docs.github.com/en/copilot)
- [JSON Processing with GSON](https://github.com/google/gson)

---

**🔥 Ready to revolutionize your academic experience with AI assistance!**

Your system now has sophisticated AI capabilities that can help with research, writing, project planning, and much more. The dual-provider setup ensures reliability, while the academic-focused prompting provides relevant, educational responses.

Need help? Check the troubleshooting section or run the diagnostic tools included in the project.
