<script setup lang="ts">
import Highlighted from '@/components/Highlighted.vue'
import GeneratorToggleField from '@/components/tools/utility/GeneratorToggleField.vue'
import KeyValueListField from '@/components/tools/utility/KeyValueListField.vue'
import StringListField from '@/components/tools/utility/StringListField.vue'
import UtilityResultActions from '@/components/tools/utility/UtilityResultActions.vue'
import UtilityShell from '@/components/tools/utility/UtilityShell.vue'
import InputText from 'primevue/inputtext'
import Select from 'primevue/select'
import { computed } from 'vue'
import { useStorage } from '@vueuse/core'

import { toKeyValueEntries, toStringEntries, type KeyValueEntry } from '@/utils/container-generators.ts'

const props = defineProps<{
  server: 'caddy' | 'nginx' | 'apache'
}>()

type SiteMode = 'reverse-proxy' | 'static-site'
type TlsMode = 'auto' | 'manual'

const mode = useStorage<SiteMode>(`pastefy-${props.server}-config-mode`, 'reverse-proxy')
const hostEntries = useStorage<string[]>(`pastefy-${props.server}-config-hosts`, ['example.com'])
const enableTls = useStorage(`pastefy-${props.server}-config-tls-enabled`, true)
const tlsMode = useStorage<TlsMode>(`pastefy-${props.server}-config-tls-mode`, 'auto')
const tlsEmail = useStorage(`pastefy-${props.server}-config-tls-email`, 'ops@example.com')
const certPath = useStorage(`pastefy-${props.server}-config-cert-path`, '/etc/letsencrypt/live/example.com/fullchain.pem')
const keyPath = useStorage(`pastefy-${props.server}-config-key-path`, '/etc/letsencrypt/live/example.com/privkey.pem')
const enableWwwRedirect = useStorage(`pastefy-${props.server}-config-www`, true)
const enableCompression = useStorage(`pastefy-${props.server}-config-compression`, true)
const enableSecurityHeaders = useStorage(`pastefy-${props.server}-config-security`, true)
const enableHsts = useStorage(`pastefy-${props.server}-config-hsts`, true)
const enableStaticCaching = useStorage(`pastefy-${props.server}-config-cache`, true)
const enableSpaFallback = useStorage(`pastefy-${props.server}-config-spa`, false)
const enableBasicAuth = useStorage(`pastefy-${props.server}-config-basic-auth`, false)
const basicAuthUser = useStorage(`pastefy-${props.server}-config-basic-auth-user`, 'deploy')
const basicAuthPasswordHash = useStorage(`pastefy-${props.server}-config-basic-auth-hash`, '$2a$14$replace-me')
const rootPath = useStorage(`pastefy-${props.server}-config-root`, '/var/www/example')
const upstreamHost = useStorage(`pastefy-${props.server}-config-upstream-host`, '127.0.0.1')
const upstreamPort = useStorage(`pastefy-${props.server}-config-upstream-port`, '3000')
const trustedProxyEntries = useStorage<string[]>(`pastefy-${props.server}-config-trusted-proxies`, [])
const forwardedIpHeader = useStorage(`pastefy-${props.server}-config-forwarded-ip-header`, 'X-Forwarded-For')
const forwardClientIp = useStorage(`pastefy-${props.server}-config-forwarded-ip-enabled`, true)
const extraHeaderEntries = useStorage<KeyValueEntry[]>(`pastefy-${props.server}-config-extra-headers`, [
  { key: 'X-Robots-Tag', value: 'noindex' },
])
const accessLogPath = useStorage(`pastefy-${props.server}-config-access-log`, `/var/log/${props.server}/access.log`)
const errorLogPath = useStorage(`pastefy-${props.server}-config-error-log`, `/var/log/${props.server}/error.log`)

const modeOptions = [
  { label: 'Reverse proxy', value: 'reverse-proxy' as SiteMode },
  { label: 'Static site', value: 'static-site' as SiteMode },
]

const tlsModeOptions = [
  { label: "Automatic / Let's Encrypt", value: 'auto' as TlsMode },
  { label: 'Manual certificate paths', value: 'manual' as TlsMode },
]

const hosts = computed(() => {
  const entries = toStringEntries(hostEntries.value)
  return entries.length ? entries : ['example.com']
})

const primaryHost = computed(() => hosts.value[0] || 'example.com')
const trustedProxies = computed(() => toStringEntries(trustedProxyEntries.value))
const headerEntries = computed(() => toKeyValueEntries(extraHeaderEntries.value))

const caddyHeaderLines = computed(() => [
  ...(enableSecurityHeaders.value
    ? [
        '    X-Frame-Options SAMEORIGIN',
        '    X-Content-Type-Options nosniff',
        '    Referrer-Policy strict-origin-when-cross-origin',
        ...(enableHsts.value && enableTls.value
          ? ['    Strict-Transport-Security "max-age=31536000; includeSubDomains"']
          : []),
      ]
    : []),
  ...headerEntries.value.map((entry) => `    ${entry.key} ${entry.value}`),
])

const nginxHeaderLines = computed(() => [
  ...(enableSecurityHeaders.value
    ? [
        '  add_header X-Frame-Options "SAMEORIGIN" always;',
        '  add_header X-Content-Type-Options "nosniff" always;',
        '  add_header Referrer-Policy "strict-origin-when-cross-origin" always;',
        ...(enableHsts.value && enableTls.value
          ? ['  add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;']
          : []),
      ]
    : []),
  ...headerEntries.value.map((entry) => `  add_header ${entry.key} "${entry.value}" always;`),
])

const apacheHeaderLines = computed(() => [
  ...(enableSecurityHeaders.value
    ? [
        '  Header always set X-Frame-Options "SAMEORIGIN"',
        '  Header always set X-Content-Type-Options "nosniff"',
        '  Header always set Referrer-Policy "strict-origin-when-cross-origin"',
        ...(enableHsts.value && enableTls.value
          ? ['  Header always set Strict-Transport-Security "max-age=31536000; includeSubDomains"']
          : []),
      ]
    : []),
  ...headerEntries.value.map((entry) => `  Header always set ${entry.key} "${entry.value}"`),
])

const buildCaddySite = (host: string) => {
  const target = `${upstreamHost.value}:${upstreamPort.value}`
  const reverseProxy = mode.value === 'reverse-proxy'
  const autoHttpsDirective = enableTls.value
    ? tlsMode.value === 'auto'
      ? `\n  tls ${tlsEmail.value}`
      : `\n  tls ${certPath.value} ${keyPath.value}`
    : '\n  auto_https disable'

  const proxyHeaders = reverseProxy && forwardClientIp.value
    ? `\n    header_up X-Real-IP {remote_host}\n    header_up ${forwardedIpHeader.value} {remote_host}\n    header_up X-Forwarded-Proto {scheme}`
    : ''

  return `${host} {${enableCompression.value ? '\n  encode zstd gzip' : ''}${autoHttpsDirective}
  log {
    output file ${accessLogPath.value}
  }${reverseProxy
    ? `\n  reverse_proxy ${target} {${proxyHeaders}\n  }`
    : `\n  root * ${rootPath.value}\n  ${enableSpaFallback.value ? 'try_files {path} /index.html' : ''}\n  file_server`}${caddyHeaderLines.value.length ? `\n\n  header {\n${caddyHeaderLines.value.join('\n')}\n  }` : ''}${enableBasicAuth.value ? `\n\n  basic_auth {\n    ${basicAuthUser.value} ${basicAuthPasswordHash.value}\n  }` : ''}\n}`
}

const buildNginxServer = (host: string) => {
  const target = `${upstreamHost.value}:${upstreamPort.value}`
  const proxyForwarding = forwardClientIp.value
    ? `\n    proxy_set_header X-Real-IP $remote_addr;\n    proxy_set_header ${forwardedIpHeader.value} $proxy_add_x_forwarded_for;\n    proxy_set_header X-Forwarded-Proto $scheme;`
    : ''

  return `server {
  listen ${enableTls.value ? '443 ssl http2' : '80'};
  server_name ${host};
  ${enableTls.value ? `ssl_certificate ${tlsMode.value === 'manual' ? certPath.value : `/etc/letsencrypt/live/${host}/fullchain.pem`};
  ssl_certificate_key ${tlsMode.value === 'manual' ? keyPath.value : `/etc/letsencrypt/live/${host}/privkey.pem`};` : ''}
  access_log ${accessLogPath.value};
  error_log ${errorLogPath.value};
  ${enableCompression.value ? 'gzip on;' : ''}${nginxHeaderLines.value.length ? `\n${nginxHeaderLines.value.join('\n')}` : ''}${trustedProxies.value.length ? `\n${trustedProxies.value.map((entry) => `  set_real_ip_from ${entry};`).join('\n')}\n  real_ip_header ${forwardedIpHeader.value};` : ''}${enableBasicAuth.value ? `\n  auth_basic "Restricted";\n  auth_basic_user_file /etc/nginx/.htpasswd;` : ''}${mode.value === 'reverse-proxy'
    ? `\n\n  location / {\n    proxy_pass http://${target};\n    proxy_set_header Host $host;${proxyForwarding}\n  }`
    : `\n\n  root ${rootPath.value};\n  index index.html;\n  location / {\n    ${enableSpaFallback.value ? 'try_files $uri $uri/ /index.html;' : 'try_files $uri $uri/ =404;'}\n  }${enableStaticCaching.value ? `\n  location ~* \\.(css|js|png|jpg|jpeg|gif|svg|ico|woff2?)$ {\n    expires 7d;\n    add_header Cache-Control "public, immutable";\n  }` : ''}`}
}`
}

const buildApacheVirtualHost = (host: string, tls: boolean) => {
  const target = `${upstreamHost.value}:${upstreamPort.value}`
  const body = mode.value === 'reverse-proxy'
    ? `ProxyPreserveHost On
  ProxyPass / http://${target}/
  ProxyPassReverse / http://${target}/`
    : `DocumentRoot ${rootPath.value}
  <Directory ${rootPath.value}>
    AllowOverride All
    Require all granted
    ${enableSpaFallback.value ? 'FallbackResource /index.html' : ''}
  </Directory>`

  const forwardedHeaders = forwardClientIp.value
    ? `\n  RequestHeader set X-Real-IP expr=%{REMOTE_ADDR}\n  RequestHeader set ${forwardedIpHeader.value} expr=%{REMOTE_ADDR}\n  RequestHeader set X-Forwarded-Proto expr=%{REQUEST_SCHEME}`
    : ''

  return `<VirtualHost *:${tls ? '443' : '80'}>
  ServerName ${host}${tls
    ? `
  SSLEngine on
  SSLCertificateFile ${tlsMode.value === 'manual' ? certPath.value : `/etc/letsencrypt/live/${host}/fullchain.pem`}
  SSLCertificateKeyFile ${tlsMode.value === 'manual' ? keyPath.value : `/etc/letsencrypt/live/${host}/privkey.pem`}`
    : ''}
  ErrorLog ${errorLogPath.value}
  CustomLog ${accessLogPath.value} combined
  ${body}${enableCompression.value ? '\n  AddOutputFilterByType DEFLATE text/html text/plain text/css application/javascript application/json' : ''}${apacheHeaderLines.value.length ? `\n${apacheHeaderLines.value.join('\n')}` : ''}${forwardedHeaders}${trustedProxies.value.length ? `\n  RemoteIPHeader ${forwardedIpHeader.value}\n${trustedProxies.value.map((entry) => `  RemoteIPTrustedProxy ${entry}`).join('\n')}` : ''}${enableBasicAuth.value ? `
  <Location />
    AuthType Basic
    AuthName "Restricted"
    AuthUserFile /etc/apache2/.htpasswd
    Require valid-user
  </Location>` : ''}
</VirtualHost>`
}

const config = computed(() => {
  if (props.server === 'caddy') {
    const redirectBlock = enableWwwRedirect.value
      ? `www.${primaryHost.value} {\n  redir ${enableTls.value ? `https://${primaryHost.value}` : `http://${primaryHost.value}`}{uri} permanent\n}`
      : ''

    return [redirectBlock, ...hosts.value.map(buildCaddySite)].filter(Boolean).join('\n\n')
  }

  if (props.server === 'nginx') {
    const redirectBlocks = [
      enableWwwRedirect.value
        ? `server {\n  listen 80;\n  server_name www.${primaryHost.value};\n  return 301 ${enableTls.value ? 'https' : 'http'}://${primaryHost.value}$request_uri;\n}`
        : '',
      ...(enableTls.value
        ? hosts.value.map(
            (host) =>
              `server {\n  listen 80;\n  server_name ${host};\n  return 301 https://${host}$request_uri;\n}`,
          )
        : []),
    ].filter(Boolean)

    return [...redirectBlocks, ...hosts.value.map(buildNginxServer)].join('\n\n')
  }

  const blocks = [
    enableWwwRedirect.value
      ? `<VirtualHost *:80>
  ServerName www.${primaryHost.value}
  Redirect permanent / ${enableTls.value ? `https://${primaryHost.value}/` : `http://${primaryHost.value}/`}
</VirtualHost>`
      : '',
    ...(!enableTls.value
      ? hosts.value.map((host) => buildApacheVirtualHost(host, false))
      : hosts.value.flatMap((host) => [
          `<VirtualHost *:80>
  ServerName ${host}
  Redirect permanent / https://${host}/
</VirtualHost>`,
          buildApacheVirtualHost(host, true),
        ])),
  ].filter(Boolean)

  return blocks.join('\n\n')
})

const resultFileName = computed(() => (props.server === 'caddy' ? 'Caddyfile' : 'site.conf'))
</script>

<template>
  <UtilityShell
    :control-title="`${props.server.toUpperCase()} Config`"
    control-description="Define one or multiple hosts, TLS, proxy or static mode, forwarding headers, auth, and deployment-friendly server settings."
    result-title="Generated Config"
    result-description="A ready-to-edit multi-site config for web servers and reverse proxies."
    fill-height
  >
    <template #controls>
      <div class="grid gap-3 md:grid-cols-2">
        <div>
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Site mode</label>
          <Select v-model="mode" :options="modeOptions" option-label="label" option-value="value" fluid />
        </div>
        <div>
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">TLS mode</label>
          <Select v-model="tlsMode" :options="tlsModeOptions" option-label="label" option-value="value" fluid :disabled="!enableTls" />
        </div>
        <div v-if="enableTls && tlsMode === 'auto' && props.server === 'caddy'">
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Let's Encrypt email</label>
          <InputText v-model="tlsEmail" fluid />
        </div>
        <div v-if="enableTls && tlsMode === 'manual'">
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Certificate path</label>
          <InputText v-model="certPath" fluid />
        </div>
        <div v-if="enableTls && tlsMode === 'manual'">
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Key path</label>
          <InputText v-model="keyPath" fluid />
        </div>
        <div>
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Access log path</label>
          <InputText v-model="accessLogPath" fluid />
        </div>
        <div>
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Error log path</label>
          <InputText v-model="errorLogPath" fluid />
        </div>
        <div v-if="mode === 'static-site'">
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Root path</label>
          <InputText v-model="rootPath" fluid />
        </div>
        <div v-else>
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Upstream host</label>
          <InputText v-model="upstreamHost" fluid />
        </div>
        <div v-if="mode === 'reverse-proxy'">
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Upstream port</label>
          <InputText v-model="upstreamPort" fluid />
        </div>
        <div v-if="mode === 'reverse-proxy'">
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Forwarded IP header</label>
          <InputText v-model="forwardedIpHeader" fluid />
        </div>
      </div>

      <StringListField
        v-model="hostEntries"
        label="Hostnames / site entries"
        placeholder="example.com"
        add-label="add host"
      />

      <div class="grid gap-3 md:grid-cols-2">
        <GeneratorToggleField v-model="enableTls" label="Enable TLS / HTTPS" />
        <GeneratorToggleField v-model="enableWwwRedirect" label="Redirect www to primary domain" />
        <GeneratorToggleField v-model="enableCompression" label="Enable compression" />
        <GeneratorToggleField v-model="enableSecurityHeaders" label="Add basic security headers" />
        <GeneratorToggleField v-model="enableHsts" label="Enable HSTS" />
        <GeneratorToggleField v-model="enableBasicAuth" label="Protect with basic auth" />
        <GeneratorToggleField v-model="enableStaticCaching" label="Cache static assets" />
        <GeneratorToggleField v-if="mode === 'static-site'" v-model="enableSpaFallback" label="Enable SPA fallback" />
        <GeneratorToggleField v-if="mode === 'reverse-proxy'" v-model="forwardClientIp" label="Forward client IP headers" />
      </div>

      <div v-if="enableBasicAuth" class="grid gap-3 md:grid-cols-2">
        <div>
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Basic auth user</label>
          <InputText v-model="basicAuthUser" fluid />
        </div>
        <div>
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Password hash / placeholder</label>
          <InputText v-model="basicAuthPasswordHash" fluid />
        </div>
      </div>

      <KeyValueListField
        v-model="extraHeaderEntries"
        label="Extra headers"
        key-placeholder="X-Robots-Tag"
        value-placeholder="noindex"
      />
      <StringListField
        v-if="props.server !== 'caddy'"
        v-model="trustedProxyEntries"
        label="Trusted proxy IPs / CIDRs"
        placeholder="10.0.0.0/8"
        add-label="add proxy"
      />
    </template>

    <template #result>
      <div class="overflow-hidden rounded-xl border border-neutral-200 bg-white dark:border-neutral-700 dark:bg-neutral-900">
        <Highlighted :contents="config" :file-name="resultFileName" />
      </div>
    </template>

    <template #footer>
      <UtilityResultActions :result="config" :file-name="resultFileName" />
    </template>
  </UtilityShell>
</template>
