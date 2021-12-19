import Vue from 'vue'
import VueRouter from 'vue-router'

const Home = () => import("../views/Home.vue")
const Author = () => import("../views/Author.vue")
const Institution = () => import("../views/Institution.vue")
const Journal = () => import("../views/Journal.vue")
const Topic = () => import("../views/Topic.vue")
const Subject = () => import("../views/Subject.vue")
const Search = () => import("../views/Search.vue")
const Paper = () => import("../views/Paper.vue")
const Admin = () => import("../views/Admin.vue")
const About = () => import("../views/About.vue")
const Register = () => import("../views/Register.vue")

const User = () => import("../views/user/User.vue")
const Profile = () => import("../views/user/Profile.vue")
const Apply = () => import("../views/user/Apply.vue")
const Message = () => import("../views/user/Message.vue")
const Patenttransfer = () => import("../views/user/Patenttransfer.vue")
const Door = () => import("../views/Door.vue")
const ScholarIdentity = () => import("../views/ScholarIdentity.vue")

Vue.use(VueRouter)

const routes = [{
    path: '/',
    name: 'Home',
    component: Home
  },
  {
    path: '/author/',
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
    path: '/topic',
    name: 'Topic',
    component: Topic
  },
  {
    path: '/subject',
    name: 'Subject',
    component: Subject
  },
  {
    path: '/search',
    name: 'Search',
    component: Search
  },
  {
    path: '/paper/:id',
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
    path: '/register',
    name: 'Register',
    component: Register
  },
  {
    path: '/user',
    name: 'User',
    component: User,
    children: [{
        path: 'profile',
        name: 'Profile',
        component: Profile
      },
      {
        path: 'apply',
        name: 'Apply',
        component: Apply
      },
      {
        path: 'message',
        name: 'Message',
        component: Message
      },
      {
        path:'patenttransfer',
        name:'Patenttransfer',
        component:Patenttransfer
      }
    ]
  },
  {
    path: '/door',
    name: 'Door',
    component: Door
  },
  {
    path: '/scholarIdentity',
    name: 'ScholarIdentity',
    component: ScholarIdentity
  },
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

export default router
