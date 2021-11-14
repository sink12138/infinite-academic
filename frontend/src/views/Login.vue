<template>
<h2>登录账号</h2>
    <div class="login">
        <v-container  class="loginOverlay">
          <v-layout  align-center justify-center>
            <v-flex xs12 sm8>
                <v-card>
                    <v-card-text class="pt-4">
                        <v-form ref="form">
                            <v-text-field
                            label="账号"
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
                        <v-btn @click="submit" class="green white--text">登录</v-btn>
                    </v-card-actions>
                </v-card>
                <v-card>
                    <v-card-title>
                        <h5>没有账号?</h5>
                    </v-card-title>
                    <v-card-actions>
                        <v-btn outlined color="black"  to="/logon" class="green white--text" >注册</v-btn>
                    </v-card-actions>
                </v-card>
            </v-flex>
          </v-layout>
       </v-container>
       <v-snackbar
        v-model="snackbar"
        :vertical="vertical"
        >
      {{ message }}
      <template v-slot:action="{ attrs }">
        <v-btn
          color="indigo"
          text
          v-bind="attrs"
          @click="snackbar = false"
        >
          关闭
        </v-btn>
      </template>
    </v-snackbar>
    </div>
</template>
