<template>
  <v-row justify="center">
    <v-dialog
      v-model="dialog"
      persistent
      max-width="600px"
    >
      <template v-slot:activator="{ on, attrs }">
        <v-btn
          depressed
          height="100%"
          dark
          v-bind="attrs"
          v-on="on"
        >
          登录/注册
        </v-btn>
      </template>
      <v-card>
        <v-card-title v-if="login === true">
          <span class="headline">登录</span>
        </v-card-title>
        <v-card-title v-if="login === false">
          <span class="headline">注册</span>
        </v-card-title>
        <v-card-text v-if="login === true">
          <v-container>
            <v-row>
              <v-col cols="12">
                <v-text-field
                  label="邮箱*"
                  required
                ></v-text-field>
              </v-col>
              <v-col cols="12">
                <v-text-field
                  label="密码*"
                  type="password"
                  required
                ></v-text-field>
              </v-col>
            </v-row>
          </v-container>
        </v-card-text>
        <v-card-text v-if="login === false">
          <v-container>
            <v-row>
              <v-col cols="12">
                <v-text-field
                  label="邮箱*"
                  required
                ></v-text-field>
              </v-col>
              <v-col cols="12">
                <v-text-field
                  label="密码*"
                  type="password"
                  required
                ></v-text-field>
              </v-col>
            </v-row>
          </v-container>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn
            color="blue darken-1"
            text
            v-if="login === true"
            @click="login = false"
          >切换注册界面</v-btn>
          <v-btn
            color="blue darken-1"
            text
            v-if="login === false"
            @click="login = true"
          >切换登录界面</v-btn>
          <v-btn
            color="blue darken-1"
            text
            @click="closeDialog()"
          >关闭</v-btn>
          <v-btn
            color="blue darken-1"
            text
            v-if="login === true"
            @click="accountLogin()"
          >登录</v-btn>
          <v-btn
            color="blue darken-1"
            text
            v-if="login === false"
            @click="accountRegister()"
          >注册</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-row>
</template>

<script>
export default {
  data: () => ({
    dialog: false,
    login:true,
  }),
  methods:{
    closeDialog(){
      this.dialog = false;
      this.login = true;
    },
    accountLogin(){
      this.dialog = false;
      this.login = true;
    },
    accountRegister(){
      this.dialog = false;
      this.login = true;
    }
  }
};
</script>
