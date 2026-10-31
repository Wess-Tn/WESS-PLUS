WESS PLUS
WESS PLUS is a browser-style AI workspace that runs in a single HTML file. It combines tabbed AI conversations, voice input and output, Google search and translation, customizable shortcuts, themes, and an interface available in more than 80 languages.
Features
Tabbed AI chat. Each tab is a separate conversation. Replies stream in as they are generated, and conversations are saved in the browser.
Voice mode. Speak to the assistant and hear its replies. After each reply the app listens again automatically.
Address bar. Type a query and press Enter to search Google. Type a domain such as youtube.com to open it. Press Shift + Enter to send the text to the assistant. Suggestions appear under the bar as you type.
Shortcuts. Pinned sites (YouTube, Discord, Facebook, Google by default) can be opened, added, or removed. The assistant can also add shortcuts or offer links in a conversation.
Translation. The translator opens Google Translate with your text, in any language pair. The "Translate this page" action opens the page typed in the address bar through Google Translate.
Languages. The interface and the assistant's replies follow the selected language. Languages not in the built-in list can be added by name, and the interface is translated on first use and cached.
Themes. Upload an image, GIF, or video as a background. Each theme can adjust darkness, blur, and fit, and includes its own color palette. Built-in palettes: Cyber, Ocean, Sunset, Matrix, and Paper.
Appearance. Dark, light, or automatic (follows the device setting).
Media. Attach images, GIFs, and videos to a conversation, or drag and drop or paste them. Media files are shown in the conversation but are not stored between sessions.
Sketches. In image mode the assistant draws a quick SVG sketch. A link opens a dedicated image generator for photographic results.
Getting started
Clone or download the repository.
Open wess-plus-v3.html in a modern browser (Chrome or Safari recommended for voice input).
Opening the file directly works for the interface, search, shortcuts, themes, and languages. The AI features need the assistant runtime described below.
How the AI works
The app calls window.claude.use("sample") to get a text-generation function. That object is provided by the Claude artifact runtime. Outside that runtime, the assistant, sketches, and automatic language translation are unavailable and show a short message. To use the assistant elsewhere, replace the getSample() function with a call to your own model API. The rest of the app only expects a function that accepts a message array and optional settings, and returns { text }.
Limitations
Embedding websites. Most sites (including Google, YouTube, Facebook, and Discord) refuse to be shown inside other pages. Shortcuts and search results therefore open in a new tab.
Remote media. Images and videos linked from the internet are not displayed inside the page, because the page's security policy blocks external media. Upload files instead.
Real image generation. The built-in sketch is a simple SVG drawing. Photographic images require an external image service.
Discord. The app can draft a server layout as text for you to paste into Discord. It does not create channels, which would need a bot and server.
Logo path. The logo is loaded from an asset path that exists only inside the google artifact host. For self-hosting, replace the /_blob/... URLs with a local file such as logo.png.
Speech. Voice input depends on the browser's Web Speech API and microphone permission. Some embedded previews block microphone access.
Data stored in the browser
The app uses localStorage only. Nothing is sent to a server by the app itself.
Key
Contents
wess_chats
Conversation text and tab state (media is not saved)
wess_sites
Shortcuts
wess_prefs
Language, appearance, reply speech, quality, visual effects
wess_themes
Themes, including background data and palettes
wess_i18n_<code>
Cached interface translations
wess_dc
Internal flag for a one-time shortcut update
Clear these keys to reset the app.
Project structure
wess-plus-v3.html   The whole application (HTML, CSS, JavaScript)
README.md           This file
Older drafts from earlier iterations may be present in the repository. wess-plus-v3.html is the current version.
Contributing
Issues and pull requests are welcome. Please keep the app in a single file unless there is a strong reason to split it, and test voice, theme, and translation changes in at least two browsers.
License
No license has been specified yet.
