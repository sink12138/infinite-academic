import Vue from 'vue'
import VueRouter from 'vue-router'

const Home = () => import("../views/Home.vue")
const Author = () => import("../views/Author.vue")
const Institution = () => import("../views/Institution.vue")
const Journal = () => import("../views/Journal.vue")
const Search = () => import("../views/Search.vue")
const Paper = () => import("../views/Paper.vue")
const Admin = () => import("../views/Admin.vue")
const About = () => import("../views/About.vue")
const Login = () => import("../views/Login.vue");
const Logon = () => import("../views/Logon.vue");
Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home
  },
  {
    path: '/author',
    name: 'Author',
    component: Author
  },
  {
    path: '/institution',
    name: 'Institution',
    component: Institution
  },
  {
    path: '/journal',
    name: 'Journal',
    component: Journal
  },
  {
    path: '/search',
    name: 'Search',
    component: Search
  },
  {
    path: '/paper',
    name: 'Paper',
    component: Paper
  },
  {
    path: '/admin',
    name: 'Admin',
    component: Admin
  },
  {
    path: '/about',
    name: 'About',
    component: About
  },
  {
    path:'/login',
    name:'Login',
    component: Login
  },
  {
    path:'/logon',
    name:'Logon',
    component: Logon
  }
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

export default router
