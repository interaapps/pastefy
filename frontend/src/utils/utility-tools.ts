export type UtilityToolDefinition = {
  slug: string
  title: string
  shortTitle: string
  description: string
  icon: string
  category:
    | 'Inspect'
    | 'Generate'
    | 'Security'
    | 'Automation'
    | 'Encode'
    | 'Time'
    | 'Visualize'
    | 'Operations'
    | 'Server'
    | 'Deploy'
  keywords: string[]
}

export const utilityTools: UtilityToolDefinition[] = [
  {
    slug: 'data-to-mermaid',
    title: 'Data to Mermaid Charts',
    shortTitle: 'Mermaid Charts',
    description:
      'Turn JSON, YAML, or CSV datasets into Mermaid pie, bar, line, grouped, and combo charts with field mapping and aggregation.',
    icon: 'chart-bar',
    category: 'Visualize',
    keywords: ['mermaid', 'chart', 'csv', 'json', 'yaml', 'visualize', 'diagram'],
  },
  {
    slug: 'mermaid-theme-builder',
    title: 'Mermaid Theme Builder',
    shortTitle: 'Theme Builder',
    description:
      'Tune Mermaid colors, fonts, spacing, and diagram styling, then export the themed config plus diagram.',
    icon: 'palette',
    category: 'Visualize',
    keywords: ['mermaid', 'theme', 'diagram', 'colors', 'styling'],
  },
  {
    slug: 'data-to-mermaid-er',
    title: 'Data to Mermaid ER Diagram',
    shortTitle: 'Data -> ER',
    description:
      'Infer Mermaid ER diagrams from JSON, YAML, or CSV schema-like data structures.',
    icon: 'chart-arcs-3',
    category: 'Visualize',
    keywords: ['mermaid', 'er diagram', 'json schema', 'yaml schema', 'csv'],
  },
  {
    slug: 'sql-to-mermaid-er',
    title: 'SQL to Mermaid ER',
    shortTitle: 'SQL -> ER',
    description:
      'Parse CREATE TABLE statements into Mermaid ER diagrams with tables and foreign keys.',
    icon: 'database',
    category: 'Visualize',
    keywords: ['sql', 'er diagram', 'mermaid', 'database', 'schema'],
  },
  {
    slug: 'infra-to-mermaid',
    title: 'Infra to Mermaid',
    shortTitle: 'Infra',
    description:
      'Convert Compose, Kubernetes, Terraform, and GitHub Actions definitions into Mermaid architecture flowcharts.',
    icon: 'hierarchy-3',
    category: 'Visualize',
    keywords: ['infrastructure', 'mermaid', 'docker compose', 'kubernetes', 'terraform', 'github actions'],
  },
  {
    slug: 'markdown-to-mermaid',
    title: 'Markdown to Mermaid Mindmap / Flow',
    shortTitle: 'Markdown -> Mermaid',
    description:
      'Turn Markdown headings and bullet outlines into Mermaid mindmaps or flowcharts.',
    icon: 'markdown',
    category: 'Visualize',
    keywords: ['markdown', 'mermaid', 'mindmap', 'flowchart', 'outline'],
  },
  {
    slug: 'data-to-class-diagram',
    title: 'JSON / YAML to Class Diagram',
    shortTitle: 'Data -> Class',
    description:
      'Map nested structured data to Mermaid class diagrams with inferred fields and relationships.',
    icon: 'binary-tree-2',
    category: 'Visualize',
    keywords: ['json', 'yaml', 'class diagram', 'mermaid', 'schema'],
  },
  {
    slug: 'mermaid-image-export',
    title: 'Mermaid Image Export Tool',
    shortTitle: 'Mermaid Export',
    description:
      'Export Mermaid diagrams as PNG or SVG with padding, scale, background, and watermark options.',
    icon: 'photo-down',
    category: 'Visualize',
    keywords: ['mermaid', 'png', 'svg', 'export', 'image'],
  },
  {
    slug: 'diff-lab',
    title: 'Diff Lab',
    shortTitle: 'Diff',
    description:
      'Compare two textareas, inspect a rendered diff, and export the unified patch.',
    icon: 'git-compare',
    category: 'Inspect',
    keywords: ['diff', 'compare', 'patch', 'unified diff', 'changes'],
  },
  {
    slug: 'jwt-inspector',
    title: 'JWT Inspector',
    shortTitle: 'JWT',
    description:
      'Decode JWTs, inspect claims, and check timestamps without sending the token anywhere.',
    icon: 'shield-lock',
    category: 'Security',
    keywords: ['jwt', 'token', 'claims', 'auth', 'bearer'],
  },
  {
    slug: 'hash-generator',
    title: 'Hash Generator',
    shortTitle: 'Hash',
    description:
      'Generate SHA hashes for text or payloads and compare lengths at a glance.',
    icon: 'fingerprint',
    category: 'Security',
    keywords: ['hash', 'sha256', 'checksum', 'digest'],
  },
  {
    slug: 'secret-generator',
    title: 'Secret Generator',
    shortTitle: 'Secrets',
    description:
      'Generate UUIDs, hex tokens, and Base64URL secrets for quick local testing and setup.',
    icon: 'sparkles',
    category: 'Generate',
    keywords: ['uuid', 'secret', 'token', 'generator', 'base64url'],
  },
  {
    slug: 'password-generator',
    title: 'Password Generator',
    shortTitle: 'Passwords',
    description:
      'Generate passwords with configurable character sets, length, count, and ambiguity rules.',
    icon: 'key',
    category: 'Security',
    keywords: ['password', 'secret', 'generator', 'security'],
  },
  {
    slug: 'cron-explainer',
    title: 'Cron Explainer',
    shortTitle: 'Cron',
    description:
      'Understand cron expressions with a readable breakdown of schedule fields and timing.',
    icon: 'clock-code',
    category: 'Automation',
    keywords: ['cron', 'schedule', 'automation', 'timing'],
  },
  {
    slug: 'base64-lab',
    title: 'Base64 Lab',
    shortTitle: 'Base64',
    description:
      'Encode and decode Base64 or Base64URL strings locally for payload and token workflows.',
    icon: 'binary',
    category: 'Encode',
    keywords: ['base64', 'base64url', 'encode', 'decode'],
  },
  {
    slug: 'url-encoder',
    title: 'URL Encoder / Decoder',
    shortTitle: 'URL Encode',
    description:
      'Encode and decode URL components and full query values without leaving the browser.',
    icon: 'link',
    category: 'Encode',
    keywords: ['url', 'encode', 'decode', 'querystring'],
  },
  {
    slug: 'timestamp-converter',
    title: 'Timestamp Converter',
    shortTitle: 'Timestamp',
    description:
      'Convert between Unix timestamps, ISO dates, and local times for debugging and docs.',
    icon: 'clock-hour-4',
    category: 'Time',
    keywords: ['timestamp', 'unix', 'iso', 'date', 'time'],
  },
  {
    slug: 'slug-generator',
    title: 'Slug Generator',
    shortTitle: 'Slug',
    description:
      'Generate slugs, snake_case, and other URL-friendly variants from plain text.',
    icon: 'writing',
    category: 'Generate',
    keywords: ['slug', 'kebab-case', 'snake_case', 'url'],
  },
  {
    slug: 'case-converter',
    title: 'Case Converter',
    shortTitle: 'Case',
    description:
      'Switch text between lowercase, uppercase, title case, camelCase, snake_case, and more.',
    icon: 'letter-case',
    category: 'Generate',
    keywords: ['case', 'camelCase', 'snake_case', 'kebab-case', 'text transform'],
  },
  {
    slug: 'query-string-parser',
    title: 'Query String Parser',
    shortTitle: 'Query',
    description:
      'Parse query strings into structured data and rebuild them cleanly.',
    icon: 'list-search',
    category: 'Inspect',
    keywords: ['query string', 'params', 'url', 'search params'],
  },
  {
    slug: 'html-entity-tool',
    title: 'HTML Entity Encoder / Decoder',
    shortTitle: 'HTML Entities',
    description:
      'Encode or decode HTML entities for templates, docs, and pasted snippets.',
    icon: 'brackets-angle',
    category: 'Encode',
    keywords: ['html entities', 'encode', 'decode', 'html'],
  },
  {
    slug: 'json-string-tool',
    title: 'JSON String Escape Tool',
    shortTitle: 'JSON String',
    description:
      'Escape text for JSON strings or unescape JSON string content back to plain text.',
    icon: 'quote',
    category: 'Encode',
    keywords: ['json', 'escape', 'string', 'unescape'],
  },
  {
    slug: 'regex-escape-tool',
    title: 'Regex Escape Tool',
    shortTitle: 'Regex Escape',
    description:
      'Escape plain text for regex patterns or unescape regex-special characters.',
    icon: 'regex',
    category: 'Encode',
    keywords: ['regex', 'escape', 'pattern'],
  },
  {
    slug: 'color-converter',
    title: 'Color Converter',
    shortTitle: 'Color',
    description:
      'Convert colors between HEX, RGB, and HSL with a live swatch preview.',
    icon: 'palette',
    category: 'Generate',
    keywords: ['color', 'hex', 'rgb', 'hsl'],
  },
  {
    slug: 'number-base-converter',
    title: 'Number Base Converter',
    shortTitle: 'Number Bases',
    description:
      'Convert values between binary, octal, decimal, and hexadecimal.',
    icon: 'binary-tree',
    category: 'Inspect',
    keywords: ['binary', 'hex', 'decimal', 'octal', 'base converter'],
  },
  {
    slug: 'text-sorter',
    title: 'Text Sorter & Deduper',
    shortTitle: 'Sorter',
    description:
      'Sort line-based lists, remove duplicates, and reverse ordering quickly.',
    icon: 'sort-ascending',
    category: 'Inspect',
    keywords: ['sort', 'dedupe', 'lines', 'text'],
  },
  {
    slug: 'lorem-ipsum-generator',
    title: 'Lorem Ipsum Generator',
    shortTitle: 'Lorem Ipsum',
    description:
      'Generate placeholder words, sentences, or paragraphs for UI and content mockups.',
    icon: 'text-plus',
    category: 'Generate',
    keywords: ['lorem ipsum', 'placeholder', 'content', 'generator'],
  },
  {
    slug: 'uuid-inspector',
    title: 'UUID Inspector',
    shortTitle: 'UUID',
    description:
      'Validate UUIDs and inspect their version, variant, and nil status.',
    icon: 'binary',
    category: 'Inspect',
    keywords: ['uuid', 'identifier', 'inspect', 'version'],
  },
  {
    slug: 'word-stats',
    title: 'Word / Line / Character Counter',
    shortTitle: 'Text Stats',
    description:
      'Count words, lines, characters, paragraphs, reading time, and other useful text metrics.',
    icon: 'align-box-left-middle',
    category: 'Inspect',
    keywords: ['word count', 'line count', 'character count', 'reading time', 'text stats'],
  },
  {
    slug: 'nginx-log-inspector',
    title: 'NGINX Logs Inspector',
    shortTitle: 'NGINX Logs',
    description:
      'Parse common NGINX access logs, inspect status codes and top paths, and export structured request data.',
    icon: 'logs',
    category: 'Operations',
    keywords: ['nginx', 'logs', 'access log', 'ops', 'http'],
  },
  {
    slug: 'apache-log-inspector',
    title: 'Apache Logs Inspector',
    shortTitle: 'Apache Logs',
    description:
      'Inspect Apache access logs with parsed requests, status groups, path summaries, and fast filtering.',
    icon: 'logs',
    category: 'Operations',
    keywords: ['apache', 'logs', 'access log', 'httpd', 'ops'],
  },
  {
    slug: 'caddy-config-generator',
    title: 'Visual Caddy File Generator',
    shortTitle: 'Caddyfile',
    description:
      'Build production-friendly Caddy configs for static sites or reverse proxies with deploy-ready defaults.',
    icon: 'server',
    category: 'Server',
    keywords: ['caddy', 'caddyfile', 'reverse proxy', 'static site', 'deploy'],
  },
  {
    slug: 'nginx-config-generator',
    title: 'Visual NGINX File Generator',
    shortTitle: 'NGINX Config',
    description:
      'Generate NGINX virtual host configs for reverse proxy and static site deployments with common production settings.',
    icon: 'server',
    category: 'Server',
    keywords: ['nginx', 'server block', 'reverse proxy', 'static site', 'deploy'],
  },
  {
    slug: 'apache-config-generator',
    title: 'Visual Apache File Generator',
    shortTitle: 'Apache Config',
    description:
      'Generate Apache VirtualHost configs with HTTPS placeholders, proxying, static hosting, logging, and caching options.',
    icon: 'server',
    category: 'Server',
    keywords: ['apache', 'virtualhost', 'reverse proxy', 'static site', 'deploy'],
  },
  {
    slug: 'dockerfile-generator',
    title: 'Visual Dockerfile Generator',
    shortTitle: 'Dockerfile',
    description:
      'Generate Dockerfiles for Node, Bun, Java JAR, Python, or static assets with useful deployment defaults.',
    icon: 'brand-docker',
    category: 'Deploy',
    keywords: ['dockerfile', 'docker', 'container', 'deploy', 'build'],
  },
  {
    slug: 'jar-start-script-generator',
    title: 'Visual JAR Start Script Generator',
    shortTitle: 'JAR Start',
    description:
      'Generate start.sh or start.bat launch scripts for Java JAR applications with JVM args, logs, and app flags.',
    icon: 'player-play',
    category: 'Deploy',
    keywords: ['java', 'jar', 'start.sh', 'start.bat', 'deploy'],
  },
  {
    slug: 'systemd-service-generator',
    title: 'Systemd Service Generator',
    shortTitle: 'systemd',
    description:
      'Generate systemd unit files for apps and services with restart policy, environment variables, and working directory setup.',
    icon: 'settings-up',
    category: 'Deploy',
    keywords: ['systemd', 'service', 'linux', 'deploy', 'daemon'],
  },
  {
    slug: 'compose-stack-generator',
    title: 'Docker Compose Web Stack Generator',
    shortTitle: 'Compose Stack',
    description:
      'Generate a simple web stack with app, proxy, Postgres, and Redis services for local or server deployment.',
    icon: 'stack-2',
    category: 'Deploy',
    keywords: ['docker compose', 'stack', 'postgres', 'redis', 'deploy'],
  },
]

export function findUtilityTool(slug?: string) {
  return utilityTools.find((tool) => tool.slug === slug)
}
