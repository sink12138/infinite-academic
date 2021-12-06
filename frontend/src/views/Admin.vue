<template>
  <div>
    <v-card class="betop">
      <BaseGoBack class="ml-6" style="float: left"></BaseGoBack>
      <div class="ma-auto d-inline-flex">
        <v-icon 
          class="ml-5 text-h2"
          color="indigo darken-4"
        >mdi-account-cog
        </v-icon>
        <div class="my-font my-5">
          Admin
        </div>
      </div>
    </v-card>



    <v-card
      v-if="this.isLogin"
      class="navigation"
      max-width="300"
      style="float:left"
    >
      <v-navigation-drawer
        permanent
        expand-on-hover
      >
        <v-list
          nav
          rounded
        >
          <v-list-item
            v-for="(item, id) in menu"
            :key="id"
            @click="setMenu(item.menu_id)"
          >
            <v-list-item-icon>
              <v-icon>{{ item.icon }}</v-icon>
            </v-list-item-icon>
            <v-list-item-title>{{ item.title }}</v-list-item-title>
          </v-list-item>
        </v-list>
      </v-navigation-drawer>
    </v-card>



    <v-container>
      <v-card
        class="welcome"
        v-if="menu_id == 0"
        flat
        outlined
      >
        <v-cart-title>
          <h1>欢迎来到管理员界面！</h1>
        </v-cart-title>
          
        
        <v-card-text>
          <v-form
            ref="userForm"
            v-model="valid"
            lazy-validation
          >
            <v-row>
              <v-text-field
                v-model="userName"
                :counter="10"
                :rules="userNameRules"
                label="用户名"
                required
                
              ></v-text-field>
            </v-row>

            <v-row>
              <v-text-field
                v-model="passWords"
                :rules="passWordsRules"
                label="密码"
                type="password"
                required
              ></v-text-field>
            </v-row>

            <v-row>
              <v-col></v-col>
              <v-btn
                v-if="!isLogin"
                color="light-blue lighten-4"
                @click="Login"
              >
                <v-icon left>
                  mdi-account-cog
                </v-icon>
                登录管理员账户
              </v-btn>
            </v-row>
          </v-form>
        </v-card-text>
      </v-card>

      <v-card
        class="personal"
        v-if="menu_id == 1"
        flat
      >
        <v-card-text>
          <div>
            欢迎进入，{{ this.userOldName }}！
          </div>
        </v-card-text>

        <v-card-actions>
          <v-form
            ref="userForm"
            v-model="valid"
            lazy-validation
          >
            <v-text-field
              v-model="userName"
              :counter="10"
              :rules="userNameRules"
              label="用户名"
              required
            ></v-text-field>

            <v-text-field
              v-model="passWords"
              :rules="passWordsRules"
              label="密码"
              type="password"
              required
            ></v-text-field>

            <v-btn
              :disabled="!valid"
              color="success"
              @click="modify"
            >
              修改
            </v-btn>
          </v-form>
        </v-card-actions>
      </v-card>

      <v-card
        v-if="menu_id == 2"
        flat
      >
        <p>欢迎来到账号管理界面！</p>
      </v-card>
      <v-card
        v-if="menu_id == 3"
        flat
      >
        <p>欢迎来到数据管理界面！</p>
      </v-card>
      <v-card
        v-if="menu_id == 4"
        flat
      >
        <p>欢迎来到数据更新界面！</p>
      </v-card>
      <v-card
        v-if="menu_id == 5"
        flat
      >
        <p>欢迎来到账号审核界面！</p>
      </v-card>
      <v-card
        v-if="menu_id == 6"
        flat
      >
        <p>欢迎来到数据审核界面！</p>
      </v-card>
      <v-card
        v-if="menu_id == 7"
        flat
      >
        <p>欢迎来到系统设置界面！</p>
      </v-card>
    </v-container>
  

  </div>
</template>

<script>
import BaseGoBack from '../components/BaseGoBack.vue'

export default {
  components: {
    BaseGoBack,
  },
  data() {
    return {
      isLogin: false,
      valid: true,
      userOldName: "管理员",
      userName: "管理员",
      userNameRules: [
        v => !!v || "用户名不能为空",
        v => (v && v.length <= 10) || "用户名不能超过10个字符",
      ],
      passWords: "123",
      passWordsRules: [
        v => !!v || "密码不能为空",
        v => (v && v.length <= 10) || "密码格式不合法",
      ],
      menu_id: 0,
      menu:[
        {
          menu_id: 1,
          title:"个人中心",
          icon:"mdi-folder",
        },
        {
          menu_id: 2,
          title:"账号管理",
          icon:"mdi-folder",
        },
        {
          menu_id: 3,
          title:"数据管理",
          icon:"mdi-folder",
        },
        {
          menu_id: 4,
          title:"数据更新",
          icon:"mdi-folder",
        },
        {
          menu_id: 5,
          title:"账号审核",
          icon:"mdi-folder",
        },
        {
          menu_id: 6,
          title:"数据审核",
          icon:"mdi-folder",
        },
        {
          menu_id: 7,
          title:"系统设置",
          icon:"mdi-folder",
        },
      ],
    }
  },
  methods:{
    setMenu(i) {
      this.menu_id = i;
      console.log(i)
    },
    Login() {
      let token = window.localStorage.token;
      this.$axios({
        method: "post",
        url: "/admin/login",
        params: {
          username: this.userName,
          password: this.passWords,
        },
        headers:{
          'token':token
        }
      }).then((response) => {
        console.log(response.data);
        if (response.data.success === true) {
          this.$notify({
            title: "成功",
            message: "登陆成功",
            type: "success",
          });
          this.isLogin = true;
          this.menu_id = 1;
        } else {
          this.$notify({
            title: "失败",
            message: "登录名或密码错误",
            type: "warning",
          });
        }
      });
    },
    modify() {
      this.userOldName = this.userName;
      this.$notify({
            title: "修改成功",
            message: "修改成功！",
            type: "success",
      });
    }
    /*
    getInfo(){
      this.$axios({
        method: "get",
        url: "api/search/info/researcher/"+this.id,
        params: {
          id: this.id
        }
      }).then(response => {
        console.log(response.data)
        this.avatarUrl=response.data.avatarUrl
        this.citationNum=response.data.citationNum
        this.name=response.data.name
        this.position=response.data.position
        this.interests=response.data.interests
        this.email=response.data.email
        this.hIndex=response.data.gIndex
        this.paperNum=response.data.paperNum
        this.patentNum=response.data.patentNum
        this.currentInst=response.data.currentInst
      }).catch(error => {
        console.log(error)
      })
    }*/
  }
}
</script>

<style>

</style>