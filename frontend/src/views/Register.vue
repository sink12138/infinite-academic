<template>
  <div class="login">
    <v-container class="loginOverlay">
      <v-layout
        align-center
        justify-center
      >
        <v-flex
          xs12
          sm8
        >
          <v-card>
            <v-card-text class="pt-4">
              <v-form ref="form">
                <v-text-field
                  label="邮箱"
                  v-model="email"
                  required
                  append-icon="fa-info-circle"
                ></v-text-field>
                <v-text-field
                  label="用户名"
                  v-model="username"
                  required
                  append-icon="fa-info-circle"
                ></v-text-field>
                <v-text-field
                  label="密码"
                  v-model="password"
                  :append-icon="show1 ? 'mdi-eye' : 'mdi-eye-off'"
                  :type="show1 ? 'text' : 'password'"
                  required
                ></v-text-field>
              </v-form>
            </v-card-text>
            <v-card-actions>
              <v-btn
                @click="Register()"
                class="green white--text"
              >注册</v-btn>
            </v-card-actions>
          </v-card>
        </v-flex>
      </v-layout>
    </v-container>
  </div>
</template>

<script>
import { sha256 } from "js-sha256";
export default {
  mounted(){
    console.log('123456',sha256('123456'))
  },
  data: () => ({
    snackbar: false,
    message: "",
    vertical: true,
    show1: false,
    username: "",
    password: "",
    email: "",
  }),
};
</script>

<style scoped>
.login {
  height: 100vh;
  background-color: #ffffff;
}
h4 {
  font-size: 1em !important;
  padding: 1em;
  color: black;
}
.v-card {
  text-align: center;
  margin: 1em;
}
.v-card__title {
  justify-content: center;
}
.v-btn {
  width: 100%;
}
.v-card {
  padding: 1em;
}
.green {
  margin-bottom: 1em;
}
.create {
  text-transform: capitalize;
  margin-bottom: 1em;
}
</style>
