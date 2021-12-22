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
              <v-col>
                <v-select
                  :items="types"
                  v-model="type"
                  label="请选择认证方式"
                  solo
                ></v-select>
              </v-col>
            </v-row>
            <v-row v-if="type.length!=0">
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
            <div v-if="type=='新建门户'">
              <v-row>
                <v-col cols="6">
                  <v-text-field
                    v-model="name"
                    :rules="idRules"
                    label="姓名"
                    required
                  ></v-text-field>
                  </v-col>
              </v-row>
              <v-row>
                <v-col v-for="i in interests.length" :key="i" cols="6">
                  <v-row>
                    <v-col>
                      <v-text-field
                        label="研究方向"
                        v-model="interests[i-1]"
                      ></v-text-field>
                    </v-col>
                    <v-col>
                      <v-btn @click="deleteIntrest(i-1)">
                        删除该项
                      </v-btn>
                    </v-col>
                  </v-row>
                </v-col>
              </v-row>
              <v-row>
                <v-col>
                  <v-btn @click="addIntrest()">
                    添加研究方向
                  </v-btn>
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
                <v-col cols="3">
                <br/>
                  <v-btn @click="find('机构','cur')">
                    查找现工作单位
                  </v-btn>
                </v-col>
                <v-col cols="3">
                  <v-switch v-model="editCur" label='自行编辑工作单位'></v-switch>
                </v-col>
                <v-col cols="6">
                  <v-text-field
                    v-model="currentInst.name"
                    label="现工作单位名称"
                    :disabled="!editCur"
                    required
                  ></v-text-field>
                </v-col>
              </v-row>
              <v-row v-for="i in institutions.length" :key="i">
                <v-col cols="3">
                <br/>
                  <v-btn @click="find('机构','ins'+(i-1))">
                    查找曾工作单位
                  </v-btn>
                </v-col>
                <v-col cols="3">
                  <v-switch v-model="editIns[i-1]" label='自行编辑工作单位'></v-switch>
                </v-col>
                <v-col cols="4">
                  <v-text-field
                    v-model="institutions[i-1].name"
                    label="曾工作单位名称"
                    :disabled="!editIns[i-1]"
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
            </div>
            <div v-if="type=='认领已有门户'">
              <v-row v-for="i in portals.length" :key="i">
                <v-col cols="3">
                  <p>{{portalsName[i-1]}}</p>
                </v-col>
                <v-col cols="3">
                  <v-btn @click="find('科研人员','aut'+(i-1))">
                    查找门户
                  </v-btn>
                </v-col>
                <v-col cols="2">
                  <v-btn @click="deleteP(i-1)">
                    删除该项
                  </v-btn>
                </v-col>
              </v-row>
              <v-btn @click="addProtal()">
                添加希望认领的门户
              </v-btn>
            </div>
            <div v-if="type!=''">
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
            
          </div>
          <br/>
        </v-card>
      </v-container>
      <br/>
    </div>

    <v-dialog v-model="getID" persistent width=1500px >
      <v-card height=5000px>
        <Search
          :fromDoor="disabled"
          :todo="todo"
          @closeID="closeID"
          @findResult="findResult"
        ></Search>
      </v-card>
    </v-dialog>
    <v-snackbar
      v-model="snackbarEmail"
      top
      timeout=3000
      color="red"
    >
      请输入真实的邮箱号
    </v-snackbar>
    <v-snackbar
      v-model="snackbarSub"
      top
      timeout=3000
      color="green"
    >
      申请成功!
    </v-snackbar>
  </div>
</template>

<script>
  import Search from '../Search.vue'
  export default {
    components: {Search},
    data() {
        return {
          getID:false,
          type:"",
          types:["新建门户","认领已有门户"],
          id:"",
          name:"",
          position:"",
          interests:[],
          email:"",
          gIndex:0,
          hIndex:0,
          paperNum:0,
          patentNum:0,
          avatarUrl:"",
          websiteLink:null,
          fileToken:null,
          vertifyCode:"",
          time:0,
          portals:[],
          portalsName:[],
          snackbarEmail:false,
          snackbarSub:false,
          currentInst:{
            id:null,
            name:null
          },
          institutions:[],
          institutionNum:0,
          idRules: [(v) => !!v || "请填写姓名"],
          passwordRules: [(v) => !!v || "请填写密码"],
          emailRules: [
            (v) => !!v || "请填写邮箱",
            (v) => /.+@.+\..+/.test(v) || "邮箱格式不合法",
          ],
          todo:"全部",
          disabled:"disabled",
          editCur:false,
          editIns:[],
          itemGetM:null,
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
          this.snackbarEmail=true
        }
        
      },
      addInst(){
        this.institutions.push({
          id:null,
          name:null
        })
        this.editIns.push(false)
      },
      addProtal(){
        this.portals.push(null)
      },
      addIntrest(){
        this.interests.push("")
      },
      deleteI(index){
        this.institutions.splice(index, 1)
        this.editIns.splice(index, 1)
      },
      deleteP(index){
        this.portals.splice(index, 1)
      },
      deleteIntrest(index){
        this.interests.splice(index,1)
      },
      submit(){
        var claim={
          portals:this.portals,
        }
        var create={
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
        if(this.type=="新建门户"){
          claim=null
          if(this.editCur){
            create.currentInst.id=null
          }else{
            create.currentInst.name=null
          }
          let i=0
          for(i=0;i<create.institutions.length;i++){
            if(this.editIns[i]){
              create.institutions[i].id=null
            }else{
              create.institutions[i].name=null
            }
          }
        }else if(this.type=="认领已有门户"){
          create=null
        }
        var b={
            content:{
              claim:claim,
              code:this.vertifyCode,
              create:create
            },
            email:this.email,
            fileToken:this.fileToken,
            websiteLink:this.websiteLink
          }
        console.log(b)
        this.$axios({
          method: "post",
          url: "/api/scholar/certify",
          data: {
            content:{
              claim:claim,
              code:this.vertifyCode,
              create:create
            },
            email:this.email,
            fileToken:this.fileToken,
            websiteLink:this.websiteLink
          }
        }).then(response => {
          console.log(response.data)
          if(response.data.success){
            this.snackbarSub=true
          }
          
        }).catch(error => {
          console.log(error)
        })
      },
      closeID(msg){
        if(msg=="close")
          this.getID=false
      },
      findResult(msg){
        let str=this.itemGetM
        if(str=='cur'){
          this.currentInst=msg
        }else if(str.substring(0,3)=='ins'){
          let x=Number(str.substring(3))
          this.institutions[x]=msg
        }else if(str.substring(0,3)){
          let x=Number(str.substring(3))
          this.portals[x]=msg.id
          this.portalsName[x]=msg.name
        }
        
      },
      find(type,it){
        this.todo=type
        this.getID=true
        this.itemGetM=it
      }
    }
  }
  
</script>