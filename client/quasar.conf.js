// Configuration for your app

module.exports = function (ctx) {
  return {
    // app plugins (/src/plugins)
    plugins: [
      'i18n',
      'axios',
      // kaixin: if use lodash, 76k added, so just use our kaixin
      'kaixin'
    ],
    css: [
      'app.styl'
    ],
    extras: [
      // kaixin: exclude roboto-font
      // ctx.theme.mat ? 'roboto-font' : null,
      'material-icons',
      // kaixin: ionicons is about 120k
      ctx.theme.ios ? 'ionicons' : null
      // 'mdi',
      // 'fontawesome'
    ],
    supportIE: true,
    build: {
      // kaixin: inline-source-map is slow, but works for breakpoint in chrome
      devtool: 'inline-source-map',
      // kaixin: use this for your access path relatited to you root
      publicPath: 'public/admin',
      // kaixin: output file to java resources dir, FYI: quasar build do not clean distDir
      distDir: '../src/main/resources/public/admin',
      scopeHoisting: true,
      // kaixin: history mode need web server special config for unknown url
      vueRouterMode: 'hash',
      // vueCompiler: true,
      // gzip: true,
      // analyze: true,
      // extractCSS: false,
      extendWebpack (cfg) {
        cfg.module.rules.push({
          enforce: 'pre',
          test: /\.(js|vue)$/,
          loader: 'eslint-loader',
          exclude: /(node_modules|quasar)/
        })
      }
    },
    devServer: {
      // https: true,
      // port: 8080,
      open: true, // opens browser window automatically
      proxy: {
        '/api': {
          target: 'http://localhost:8000',
          changeOrigin: true
        }
      }
    },
    // framework: 'all' --- includes everything; for dev only!
    framework: {
      components: [
        'QLayout',
        'QLayoutHeader',
        'QLayoutDrawer',
        'QPageContainer',
        'QPage',
        'QToolbar',
        'QToolbarTitle',
        'QBtn',
        'QIcon',
        'QList',
        'QListHeader',
        'QItem',
        'QItemWrapper',
        'QItemMain',
        'QItemSide',
        'QAjaxBar',
        'QTable',
        'QTableColumns',
        'QTd',
        'QCollapsible',
        'QField',
        'QInput',
        'QInputFrame',
        'QToggle',
        'QDatetime',
        'QEditor',
        'QSearch',
        'QPopover',
        'QCheckbox',
        'QRadio',
        'QChip',
        'QSelect',
        'QUploader',
        'QModal'
      ],

      directives: [
        'Ripple'
      ],
      // Quasar plugins
      plugins: [
        'Notify',
        'Dialog'
      ],
      iconSet: ctx.theme.mat ? 'material-icons' : 'ionicons'
    },
    // animations: 'all' --- includes all animations
    animations: [
    ],
    pwa: {
      // workboxPluginMode: 'InjectManifest',
      // workboxOptions: {},
      manifest: {
        // name: 'Quasar App',
        // short_name: 'Quasar-PWA',
        // description: 'Best PWA App in town!',
        display: 'standalone',
        orientation: 'portrait',
        background_color: '#ffffff',
        theme_color: '#027be3',
        icons: [
          {
            'src': 'statics/icons/icon-128x128.png',
            'sizes': '128x128',
            'type': 'image/png'
          },
          {
            'src': 'statics/icons/icon-192x192.png',
            'sizes': '192x192',
            'type': 'image/png'
          },
          {
            'src': 'statics/icons/icon-256x256.png',
            'sizes': '256x256',
            'type': 'image/png'
          },
          {
            'src': 'statics/icons/icon-384x384.png',
            'sizes': '384x384',
            'type': 'image/png'
          },
          {
            'src': 'statics/icons/icon-512x512.png',
            'sizes': '512x512',
            'type': 'image/png'
          }
        ]
      }
    },
    cordova: {
      // id: 'org.cordova.quasar.app'
    },
    electron: {
      // bundler: 'builder', // or 'packager'
      extendWebpack (cfg) {
        // do something with Electron process Webpack cfg
      },
      packager: {
        // https://github.com/electron-userland/electron-packager/blob/master/docs/api.md#options

        // OS X / Mac App Store
        // appBundleId: '',
        // appCategoryType: '',
        // osxSign: '',
        // protocol: 'myapp://path',

        // Window only
        // win32metadata: { ... }
      },
      builder: {
        // https://www.electron.build/configuration/configuration

        // appId: 'quasar-app'
      }
    }
  }
}
