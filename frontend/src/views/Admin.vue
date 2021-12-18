<template>
  <div>
    <Banner
      :title="{
        text: this.isLogin?this.menu[this.menu_id - 1].title:'管理员',
        icon: 'mdi-account-cog',
        time: this.logoutTime
      }"
    ></Banner>

    <v-row
      justify="center"
      align="center"
    >
      <v-card
        flat
        style="top:60px"
        width="1350"
      >
        <v-navigation-drawer
          v-if="this.isLogin"
          permanent
          floating
          expand-on-hover
          fixed
          style="top:60px"
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

        <div
          class="accountManagement"
          v-if="menu_id == 2"
        >
          <AccountManagement></AccountManagement>
        </div>

        <div
          class="reviewApplication"
          v-if="menu_id == 3"
        >
          <ReviewApplication></ReviewApplication>
        </div>

        <div
          class="systemSettings"
          v-if="menu_id == 4"
        >
          <SystemSetting></SystemSetting>
        </div>
      </v-card>
    </v-row>

  </div>
</template>

<script>
import Banner from '../components/AdminBanner.vue'
import AccountManagement from '../components/AdminAccountManagement.vue'
import ReviewApplication from '../components/AdminReviewApplication.vue'
import SystemSetting from '../components/AdminSetting.vue'
import { sha256 } from "js-sha256";

export default {
  components: {
    Banner,
    AccountManagement,
    ReviewApplication,
    SystemSetting,
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
          title:"审核申请",
          icon:"mdi-check-circle-outline",
        },
        {
          menu_id: 4,
          title:"系统设置",
          icon:"mdi-cog-outline",
        },
      ],
    }
  },
  methods:{
    setMenu(i) {
      this.menu_id = i;
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
        console.log(token)
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
    modify() {
      this.userOldName = this.userName;
      this.$notify({
            title: "修改成功",
            message: "修改成功！",
            type: "success",
      });
    },
  },
}
</script>

<style>

</style>
