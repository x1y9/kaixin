
export default [
  { path: '/', component: () => import('layouts/default'), auth: true,
    children: [
      { path: '', component: () => import('pages/dashboard'), auth: true },
      { path: 'list/:model', component: () => import('pages/list'), auth: true },
      { path: 'update/:model/:entity', component: () => import('pages/update'), auth: true },
      { path: 'create/:model/:entity', component: () => import('pages/create'), auth: true }
    ]
  },
  { path: '/login', component: () => import('pages/Login') },
  // { path: '/help', component: () => import('pages/Help') },
  { path: '/error', component: () => import('pages/error') },
  { path: '*', component: () => import('pages/404') }
]
