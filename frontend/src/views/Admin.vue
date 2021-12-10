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
        <div
          class="my-font my-5"
        >
          <div 
            v-if="!isLogin"
          >
            管理员
          </div>
          <div 
            v-if="isLogin"
          >
            {{ menu[menu_id - 1].title }}
          </div>
        </div>
        
      </div>
      <div 
        v-if="isLogin"
        style="float: right"
      >
        {{ logoutTime }} 将自动登出，请注意休息喔！
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
        <v-card-title>
          <v-row 
            justify="center" 
            align="center"
          >
            <h1>欢迎来到管理员界面！</h1>
          </v-row>
        </v-card-title>
          
        <v-card-text>
          <v-form
            ref="userForm"
            v-model="valid"
            lazy-validation
          >
            <v-row 
              justify="center" 
              align="center"
            >
              <v-col 
                clos="12" 
                sm="4"
              >
                <v-text-field
                  v-model="userName"
                  :counter="10"
                  :rules="userNameRules"
                  label="用户名"
                  required
                ></v-text-field>
              </v-col>
            </v-row>

            <v-row 
              justify="center" 
              align="center"
            >
              <v-col 
                clos="12" 
                sm="4"
              >
                <v-text-field
                  v-model="passWords"
                  :rules="passWordsRules"
                  label="密码"
                  type="password"
                  required
                ></v-text-field>
              </v-col>
            </v-row>

            <v-row>
              <v-col>
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
              </v-col>
            </v-row>
          </v-form>
        </v-card-text>
      </v-card>

      <v-card
        class="personal"
        v-if="menu_id == 1"
        flat
        outlined
      >
        <v-card-title>
          <v-row 
            justify="center" 
            align="center"
          >
            <h1>欢迎进入，{{ this.userOldName }}！</h1>
          </v-row>
        </v-card-title>

        <v-card-text>
          <v-form
            ref="userModifyForm"
            v-model="valid"
            lazy-validation
          >
            <v-row 
              justify="center" 
              align="center"
            >
              <v-col 
                clos="12" 
                sm="4"
              >
                <v-text-field
                  v-model="userName"
                  :counter="10"
                  :rules="userNameRules"
                  label="用户名"
                  required
                ></v-text-field>
              </v-col>
            </v-row>

            <v-row 
              justify="center" 
              align="center"
            >
              <v-col 
                clos="12" 
                sm="4"
              >
                <v-text-field
                  v-model="passWords"
                  :rules="passWordsRules"
                  label="密码"
                  type="password"
                  required
                ></v-text-field>
              </v-col>
            </v-row>

            <v-row>
              <v-col 
                clos="12" 
                sm="4"
              ></v-col>
              <v-col 
                clos="12" 
                sm="2"
              >
                <v-btn
                  :disabled="!valid"
                  color="success"
                  @click="modify"
                >
                  修改
                </v-btn>
              </v-col>

              <v-col 
                clos="12" 
                sm="2"
              >
                <v-btn
                  color="primary"
                  @click="Logout"
                >
                  登出
                </v-btn>
              </v-col>
              <v-col 
                clos="12" 
                sm="4"
              ></v-col>
            </v-row>
          </v-form>
        </v-card-text>
      </v-card>

      <v-card
        class="accountmanagement"
        v-if="menu_id == 2"
        flat
        outlined
      >
        <v-toolbar>
          <v-row
            justify="center" 
            align="center"
          >
            <v-app-bar-nav-icon></v-app-bar-nav-icon>

            <v-text-field
              label="账号搜索"
              placeholder="请输入用户名或邮箱"
              filled
              rounded
              dense
              v-model="accountSearch"
            ></v-text-field>
            
            <v-spacer></v-spacer>

            <v-btn 
              icon
              @click="searchAccount"
            >
              <v-icon>mdi-magnify</v-icon>
            </v-btn>
          </v-row>
        </v-toolbar>   

        <v-card-text>
          <v-form
            ref="accountModifyForm"
            v-model="valid"
            lazy-validation
          >
            <v-row 
              justify="center" 
              align="center"
            >
              <v-col 
                clos="12" 
                sm="4"
              >
                <v-text-field
                  v-model="accountName"
                  :counter="10"
                  :rules="userNameRules"
                  label="用户名"
                  required
                ></v-text-field>
              </v-col>
            </v-row>

            <v-row 
              justify="center" 
              align="center"
            >
              <v-col 
                clos="12" 
                sm="4"
              >
                <v-text-field
                  v-model="accountEmail"
                  :rules="emailRules"
                  label="邮箱"
                  required
                ></v-text-field>
              </v-col>
            </v-row>

            <v-row 
              justify="center" 
              align="center"
            >
              <v-col 
                clos="12" 
                sm="4"
              >
                <v-text-field
                  v-model="accountPasswords"
                  :rules="passWordsRules"
                  label="密码"
                  required
                ></v-text-field>
              </v-col>
            </v-row>

            <v-row>
              <v-col 
                clos="12" 
                sm="4"
              ></v-col>
              <v-col 
                clos="12" 
                sm="2"
              >
                <v-btn
                  :disabled="!valid"
                  color="success"
                  @click="modify"
                >
                  修改
                </v-btn>
              </v-col>

              <v-col 
                clos="12" 
                sm="2"
              >
                <v-btn
                  color="primary"
                  @click="Logout"
                >
                  登出
                </v-btn>
              </v-col>
              <v-col 
                clos="12" 
                sm="4"
              ></v-col>
            </v-row>
          </v-form>
        </v-card-text>
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
import { sha256 } from "js-sha256";

export default {
  components: {
    BaseGoBack,
  },
  data() {
    return {
      isLogin: false,
      logoutTime: "",
      valid: true,
      userOldName: "管理员",
      userName: "admin",
      userNameRules: [
        v => !!v || "用户名不能为空",
        v => (v && v.length <= 10) || "用户名不能超过10个字符",
      ],
      passWords: "admin-academic-2021",
      passWordsRules: [
        v => !!v || "密码不能为空",
      ],
      accountSearch: "",
      accountName: "无相关账号信息",
      accountEmail: "无相关账号信息",
      accountPasswords: "无相关账号信息",
      menu_id: 0,
      menu:[
        {
          menu_id: 1,
          title:"个人中心",
          icon:"mdi-account-cog-outline",
        },
        {
          menu_id: 2,
          title:"账号管理",
          icon:"mdi-account-box-multiple-outline",
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
        url: "api/admin/login",
        params: {
          username: this.userName,
          password: sha256(this.passWords),
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
          this.userOldName = this.userName;
          this.logoutTime = new Date((new Date().getTime() + 1000 * 30 * 60)).toLocaleTimeString();
          this.leftTime = 30 * 60;
          this.timer = setInterval(() => {
            this.leftTime--;

            if (this.leftTime === 0) {
              this.Logout();
              clearInterval(this.timer);
            }
          }, 1000);
        } else {
          this.$notify({
            title: "失败",
            message: "登录名或密码错误",
            type: "warning",
          });
        }
      });
    },
    Logout() {
      let token = window.localStorage.token;
      this.$axios({
        method: "post",
        url: "api/admin/logout",
        headers:{
          'token':token
        }
      }).then((response) => {
        console.log(response.data);
        if (response.data.success === true) {
          this.$notify({
            title: "成功",
            message: "登出成功",
            type: "success",
          });
          this.isLogin = false;
          this.menu_id = 0;
          this.userOldName = "管理员";
        } else {
          this.$notify({
            title: "失败",
            message: "登出失败",
            type: "warning",
          });
        }
      });
    },
    searchAccount() {
      if(this.accountSearch.search(/@/) < 0) {
        this.accountName = this.accountSearch;
        this.accountEmail = "123@qq.com";
        this.accountPasswords = "123"
      }
      else {
        this.accountName = "用户名";
        this.accountEmail = this.accountSearch;
        this.accountPasswords = "123"
      }
    },
    isValid() {
      this.$axios({
        method: "post",
        url: "api/admin/auth",
        params: {
          username: this.userName,
          password: sha256(this.passWords),
        },
      }).then((response) => {
        console.log(response.data);
        if (response.data.success === true) {
          this.$notify({
            title: "成功",
            message: "验证成功",
            type: "success",
          });
        } else {
          this.$notify({
            title: "失败",
            message: "登出失败",
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
    
  }
}
</script>

<style>

</style>