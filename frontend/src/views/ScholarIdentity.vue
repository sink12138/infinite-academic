<template>
  <div>
    <div class="whole" style="width:60%">
      <p>欢迎您进行学者认证!</p>
      <v-container>
        <v-card
          calss="rounded-lg"
          elevation=6
          width=100%
        >
          <div class="whole">
            <v-row>
              <v-col cols="9">
                <v-text-field
                  v-model="email"
                  label="邮箱"
                  :rules="emailRules"
                  required
                ></v-text-field>
                </v-col>
                <v-col>
                <v-btn @click="getVertifyCode()" v-if="time==0">
                  发送验证码
                </v-btn>
                <v-btn disabled v-else>
                  发送验证码({{time}})
                </v-btn>
              </v-col>
            </v-row>
            <v-row>
              <v-col cols="6">
                <v-text-field
                  v-model="name"
                  :rules="idRules"
                  label="姓名"
                  required
                ></v-text-field>
                </v-col>
                <v-col>
                <v-text-field
                  v-model="interests"
                  label="研究方向"
                  required
                ></v-text-field>
              </v-col>
            </v-row>
            <v-row>
              <v-col cols="6">
                <v-text-field
                  v-model="gIndex"
                  :rules="idRules"
                  label="g指数"
                  required
                ></v-text-field>
                </v-col>
                <v-col>
                <v-text-field
                  v-model="hIndex"
                  label="h指数"
                  required
                ></v-text-field>
              </v-col>
            </v-row>
            <v-row>
              <v-col cols="6">
                <v-text-field
                  v-model="currentInst.id"
                  label="现工作单位id"
                  required
                ></v-text-field>
              </v-col>
              <v-col cols="6">
                <v-text-field
                  v-model="currentInst.name"
                  label="现工作单位名称"
                  required
                ></v-text-field>
              </v-col>
            </v-row>
            <v-row v-for="i in institutions.length" :key="i">
              <v-col cols="5">
                <v-text-field
                  v-model="institutions[i-1].id"
                  label="曾工作单位id"
                  required
                ></v-text-field>
              </v-col>
              <v-col cols="5">
                <v-text-field
                  v-model="institutions[i-1].name"
                  label="曾工作单位名称"
                  required
                ></v-text-field>
              </v-col>
              <v-col cols="2">
                <v-btn @click="deleteI(i-1)">
                  删除该项
                </v-btn>
              </v-col>
            </v-row>
            <v-btn @click="addInst()">
              新增曾工作单位
            </v-btn>
            <v-row>
              <v-col >
                <v-text-field
                  v-model="websiteLink"
                  label="证明材料链接"
                ></v-text-field>
              </v-col>
            </v-row>
            <v-row>
              <v-col>
                <v-file-input 
                chips 
                label="上传证明文件"
                ></v-file-input>
              </v-col>
            </v-row>
            <v-row v-for="i in portals.length" :key="i">
              <v-col cols="6">
                <v-text-field
                  v-model="portals[i-1]"
                  label="希望直接认领的门户id"
                  required
                ></v-text-field>
              </v-col>
              <v-col cols="2">
                <v-btn @click="deleteP(i-1)">
                  删除该项
                </v-btn>
              </v-col>
            </v-row>
            <v-btn @click="addProtal()">
              添加希望直接认领的门户
            </v-btn>
            <v-row>
              <v-col cols="6">
                <v-text-field
                  v-model="vertifyCode"
                  label="验证码"
                  required
                ></v-text-field>
              </v-col>
            </v-row>
            <v-btn @click="submit()">
              提交认证申请
            </v-btn>
          </div>
          <br/>
        </v-card>
      </v-container>
      <br/>
    </div>
    <v-snackbar
      v-model="snackbar"
      top
      timeout=3000
      color="red"
    >
      请输入真实的邮箱号
    </v-snackbar>
  </div>
</template>

<script>
  export default {
    data() {
        return {
          id:"",
          name:"",
          position:"",
          interests:"",
          email:"",
          gIndex:0,
          hIndex:0,
          paperNum:0,
          patentNum:0,
          avatarUrl:"",
          websiteLink:"",
          fileToken:"",
          vertifyCode:"",
          time:0,
          portals:[],
          snackbar:false,
          currentInst:{
            id:"",
            name:""
          },
          institutions:[],
          institutionNum:0,
          idRules: [(v) => !!v || "请填写姓名"],
          passwordRules: [(v) => !!v || "请填写密码"],
          emailRules: [
            (v) => !!v || "请填写邮箱",
            (v) => /.+@.+\..+/.test(v) || "邮箱格式不合法",
          ],
        }
    },
    methods:{
      getVertifyCode(){
        if(/.*@.*/.test(this.email)){
          this.$axios({
            method: "post",
            url: "/api/scholar/certify/code",
            params: {
              email: this.email
            }
          }).then(response => {
            console.log(response.data)
          }).catch(error => {
            console.log(error)
          })
            this.time=60
            let timer = setInterval(()=>{
              this.time-=1
              if (this.time==0) {
                clearInterval(timer);
              }
            },1000)
        }else{
          this.snackbar=true
        }
        
      },
      addInst(){
        this.institutions.push({
          id:"",
          name:""
        })
      },
      addProtal(){
        this.portals.push("")
      },
      deleteI(index){
        this.institutions.splice(index, 1)
      },
      deleteP(index){
        this.portals.splice(index, 1)
      },
      submit(){
        this.$axios({
          method: "post",
          url: "/api/scholar/certify",
          params: {
            ctfApp:{
              content:{
                claim:{
                  portals:this.portals,
                },
                code:this.vertifyCode,
                create:{
                  currentInst:{
                    id:this.currentInst.id,
                    name:this.currentInst.name
                  },
                  gIndex:this.gIndex,
                  hIndex:this.hIndex,
                  institutions:this.institutions,
                  interests:this.interests,
                  name:this.name,
                }
              },
              email:this.email,
              fileToken:this.fileToken,
              websiteLink:this.websiteLink
            }
          }
        }).then(response => {
          console.log(response.data)
        }).catch(error => {
          console.log(error)
        })
      }
    }
  }
  
</script>