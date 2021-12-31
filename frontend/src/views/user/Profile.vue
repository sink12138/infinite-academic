<template>
  <div class="center">
    <v-card flat outlined min-width="800" min-height="600" class="text-left pa-5">
      <v-card-title>个人资料</v-card-title>
      <v-row no-gutters>
        <v-col cols="4">
          <v-card-text class="font-weight-black"> 邮箱: </v-card-text>
        </v-col>
        <v-col cols="8">
          <v-card-text>
            {{ email }}
            <v-btn icon @click="changeEmail()">
              <v-icon>mdi-pencil</v-icon>
            </v-btn>
          </v-card-text>
        </v-col>
      </v-row>
      <v-divider></v-divider>
      <v-row no-gutters>
        <v-col cols="4">
          <v-card-text class="font-weight-black"> 用户名: </v-card-text>
        </v-col>
        <v-col cols="8">
          <v-card-text>
            {{ username }}
            <v-btn icon @click="changePassword()">
              <v-icon>mdi-pencil</v-icon>
            </v-btn>
          </v-card-text>
        </v-col>
      </v-row>
      <v-divider></v-divider>
      <v-row no-gutters>
        <v-col cols="4">
          <v-card-text class="font-weight-black"> 注册时间: </v-card-text>
        </v-col>
        <v-col cols="8">
          <v-card-text v-text="date"></v-card-text>
        </v-col>
      </v-row>
      <v-divider></v-divider>
      <v-row no-gutters v-if="this.reseacherId != null">
        <v-col cols="4">
          <v-card-text class="font-weight-black"> 学者主页: </v-card-text>
        </v-col>
        <v-col cols="8">
          <v-icon class="link" large @click="href('author',reseacherId)">mdi-link</v-icon>
        </v-col>
      </v-row>
    </v-card>
    <v-dialog
      v-model="dialog"
      persistent
      max-width="600px"
      v-if="dialog === true"
    >
      <v-card>
        <v-card-title>
          <span class="headline" v-if="isChangePassword === false"
            >修改邮箱</span
          >
          <span class="headline" v-if="isChangePassword">修改用户名密码</span>
        </v-card-title>
        <v-divider></v-divider>
        <v-card-text>
          <v-container>
            <v-row>
              <v-col cols="12">
                <v-text-field
                  v-model="newEmail"
                  label="邮箱*"
                  :rules="emailRules"
                  required
                  v-if="isChangePassword === false"
                ></v-text-field>
              </v-col>
              <v-col cols="12">
                <v-text-field
                  v-model="newUsername"
                  label="新用户名*"
                  :rules="passwordRules"
                  required
                  v-if="isChangePassword === true"
                ></v-text-field>
              </v-col>
              <v-col cols="12">
                <v-text-field
                  v-model="newPassword"
                  label="密码*"
                  type="password"
                  :rules="passwordRules"
                  required
                  v-if="isChangePassword === true"
                ></v-text-field>
              </v-col>
            </v-row>
          </v-container>
        </v-card-text>
        <v-card-actions>
          <v-btn
            color="blue darken-1"
            text
            @click="dialog = false"
            v-if="dialog === true"
            >返回</v-btn
          >
          <v-spacer></v-spacer>
          <v-btn
            color="blue darken-1"
            text
            @click="submitEmail()"
            v-if="isChangePassword === false"
            >修改邮箱</v-btn
          >
          <v-btn
            color="blue darken-1"
            text
            @click="submitUsernameAndPassword()"
            v-if="isChangePassword === true"
            >修改</v-btn
          >
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script>
import { sha256 } from "js-sha256";
export default {
  mounted() {
    this.$axios({
      method: "get",
      url: "/api/account/profile",
    }).then((response) => {
      console.log(response.data);
      if (response.data.success === true) {
        this.date = response.data.data.date;
        this.username = response.data.data.username;
        this.email = response.data.data.email;
        this.reseacherId = response.data.data.reseacherId;
      }
    });
  },
  data() {
    return {
      reseacherId:"",
      username: "",
      email: "",
      date: "",
      newEmail: "",
      newPassword: "",
      newUsername: "",
      dialog: false,
      isChangePassword: false,
      emailRules: [
        (v) => !!v || "邮箱未填写",
        (v) => /.+@.+/.test(v) || "邮箱无效",
      ],
      passwordRules: [(v) => !!v || "密码未填写"],
    };
  },
  methods: {
    changeEmail() {
      this.dialog = true;
      this.isChangePassword = false;
    },
    changePassword() {
      this.dialog = true;
      this.isChangePassword = true;
    },
    submitEmail() {
      if (this.newEmail === "") {
        this.$notify({
          title: "错误",
          message: "邮箱未填写",
          type: "warning",
        });
      } else {
        this.$axios({
          method: "post",
          url: "/api/account/profile/modify/email",
          params: {
            email: this.newEmail,
          },
        }).then((response) => {
          console.log(response.data);
          if (response.data.success) {
            location.reload();
            this.$notify({
              title: "成功",
              message: "请到邮箱中验证后修改成功",
              type: "success",
            });
            this.dialog = false;
          } else {
            this.$notify({
              title: "失败",
              message: "修改失败",
              type: "warning",
            });
          }
        });
      }
    },
    submitUsernameAndPassword() {
      if (this.newUsername === "") {
        this.$notify({
          title: "错误",
          message: "用户名未填写",
          type: "warning",
        });
      } else if (this.newPassword === "") {
        this.$notify({
          title: "错误",
          message: "密码未填写",
          type: "warning",
        });
      } else {
        this.$axios({
          method: "post",
          url: "/api/account/profile/modify/info",
          params: {
            username: this.newUsername,
            password: sha256(this.newPassword),
          },
        }).then((response) => {
          console.log(this.newUsername);
          console.log(this.newPassword);
          console.log(response.data);
          if (response.data.success) {
            this.$notify({
              title: "成功",
              message: "修改成功",
              type: "success",
            });
            this.dialog = false;
            location.reload();
          } else {
            this.$notify({
              title: "失败",
              message: "修改失败",
              type: "warning",
            });
          }
        });
      }
    },
    href(type, id) {
      if (id == null) {
        this.$notify({
          title: "数据缺失",
          message: "信息暂未收录，给您带来不便敬请谅解。",
          type: "warning",
        });
        return;
      }
      let { href } = this.$router.resolve({ path: type, query: { id: id } });
      window.open(href, "_blank");
    },
  },
};
</script>

<style>
.center {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100%;
}
</style>
