<template>
  <div>
    <v-simple-table>
      <template v-slot:default>
        <thead>
          <tr>
            <th>标签</th>
            <th>信息</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <td>邮箱</td>
            <td>{{ email }}</td>
          </tr>
          <tr>
            <td>用户名</td>
            <td>{{ username }}</td>
          </tr>
          <tr>
            <td>注册时间</td>
            <td>{{ date }}</td>
          </tr>
          <tr>
            <td>修改邮箱</td>
            <v-btn
              color="primary"
              @click="changeEmail()"
            >修改邮箱</v-btn>
          </tr>
          <tr>
            <td>修改用户名和密码</td>
            <v-btn
              color="primary"
              @click="changePassword()"
            >修改用户名密码</v-btn>
          </tr>
        </tbody>
      </template>
    </v-simple-table>
    <v-dialog
      v-model="dialog"
      persistent
      max-width="600px"
      v-if="dialog === true"
    >
      <v-card>
        <v-card-title>
          <span
            class="headline"
            v-if="isChangePassword === false"
          >修改邮箱</span>
          <span
            class="headline"
            v-if="isChangePassword"
          >修改邮箱</span>
        </v-card-title>
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
          >返回</v-btn>
          <v-spacer></v-spacer>
          <v-btn
            color="blue darken-1"
            text
            @click="submitEmail()"
            v-if="isChangePassword === false"
          >修改邮箱</v-btn>
          <v-btn
            color="blue darken-1"
            text
            @click="submitUsernameAndPassword()"
            v-if="isChangePassword === true"
          >修改</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script>
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
      }
    });
  },
  data() {
    return {
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
            username: this.username,
            password: this.newPassword,
          },
        }).then((response) => {
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
  },
};
</script>

<style>
</style>
