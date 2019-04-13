import HeaderAsideLayout from './layouts/HeaderAsideLayout';
import Dashboard from './pages/Dashboard';
import Drawguess from './pages/Drawguess';
import NoOneSurvived from './pages/NoOneSurvived';
import NotFound from './pages/NotFound';

const routerConfig = [
  {
    path: '/',
    layout: HeaderAsideLayout,
    component: Dashboard,
    children: [],
  },
  {
    path: '/games',
    layout: HeaderAsideLayout,
    component: Dashboard,
    children: [
      {
        path: '/games/draw_guess',
        layout: HeaderAsideLayout,
        component: Drawguess,
      },
      {
        path: '/games/no_one_survived',
        layout: HeaderAsideLayout,
        component: NoOneSurvived,
      },
    ],
  },
  {
    path: '*',
    layout: HeaderAsideLayout,
    component: NotFound,
  },
];

export default routerConfig;
