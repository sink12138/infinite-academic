<template>
  <div>
    <Banner :title="{text: 'Door', icon: 'mdi-clipboard-account'}"></Banner>
    <div class="whole">
      <v-row>
        <v-col cols="3">
          <v-card
            class="mx-auto left"
            width="256"
            height="600"
            tile
          >
            <v-navigation-drawer permanent>
              <v-system-bar></v-system-bar>
              <v-list>
                <v-list-item>
                  <v-list-item-avatar>
                    <v-img src="https://cdn.vuetifyjs.com/images/john.png"></v-img>
                  </v-list-item-avatar>
                </v-list-item>

                <v-list-item link>
                  <v-list-item-content>
                    <v-list-item-title class="title">John Leider</v-list-item-title>
                    <v-list-item-subtitle>john@vuetifyjs.com</v-list-item-subtitle>
                  </v-list-item-content>

                  <v-list-item-action>
                    <v-icon>mdi-menu-down</v-icon>
                  </v-list-item-action>
                </v-list-item>
              </v-list>
              <v-divider></v-divider>
              <v-list
                nav
                dense
              >
                <v-list-item-group color="primary">
                  <v-list-item @click="manage=0">

                    <v-list-item-content>门户管理</v-list-item-content>
                  </v-list-item>
                  <v-list-item @click="manage=1">

                    <v-list-item-content>文献管理</v-list-item-content>
                  </v-list-item>
                </v-list-item-group>
              </v-list>
            </v-navigation-drawer>
          </v-card>
        </v-col>
        <v-col v-if="manage==0" offset-md="1">
          <div style="padding-top:10px;">
            <v-row  class="mb-6">
              <v-col cols="3">
                <v-btn 
                @click="addDoor=true"
                :disabled="editingD"
                >
                  认领新的门户
                </v-btn>
              </v-col>
              <v-col cols="3">
                <v-btn 
                @click="editingD=true"
                :disabled="editingD"
                >
                  编辑门户信息
                </v-btn>
              </v-col>
            </v-row>
            <div>
              <h1 style="text-align:left">
                {{name}}
              </h1>
              <h3 style="text-align:left">职位:{{position}}</h3>
              <h3 style="text-align:left">科研方向:{{interests}}</h3>
              <h3 style="text-align:left">邮箱:{{email}}</h3>
              <h3 style="text-align:left">g指数:{{gIndex}}  h指数:{{hIndex}}</h3>
              <h3 style="text-align:left">文章数量:{{paperNum}}  专利数量:{{patentNum}}  引用次数:{{citationNum}}</h3>
              <h3 style="text-align:left">现工作单位:{{currentInst.name}}</h3>
              <h3 style="text-align:left;float:left">曾工作单位:</h3>
              <h3 style="text-align:left;float:left" v-for="i in institutions.length" :key="i">{{institutions[i-1].name}}&emsp;&emsp;</h3>
            </div>
            <!-- <div class="whole">
              <v-row>
                <v-col cols="9">
                  <v-text-field
                    v-model="email"
                    label="邮箱"
                    :rules="emailRules"
                    :disabled="!editingD"
                    required
                  ></v-text-field>
                  </v-col>
                  <v-col>
                    <div v-if="editingD">
                      <v-btn @click="getVertifyCode()" v-if="time==0">
                        发送验证码
                      </v-btn>
                      <v-btn disabled v-else>
                        发送验证码({{time}})
                      </v-btn>
                    </div>
                </v-col>
              </v-row>
              <v-row>
                <v-col cols="6">
                  <v-text-field
                    v-model="name"
                    :rules="idRules"
                    label="姓名"
                    :disabled="!editingD"
                    required
                  ></v-text-field>
                  </v-col>
                  <v-col>
                  <v-text-field
                    v-model="interests"
                    label="研究方向"
                    :disabled="!editingD"
                    required
                  ></v-text-field>
                </v-col>
              </v-row>
              <v-row>
                <v-col cols="6">
                  <v-text-field
                    v-model="gIndex"
                    label="g指数"
                    :disabled="!editingD"
                    required
                  ></v-text-field>
                  </v-col>
                  <v-col>
                  <v-text-field
                    v-model="hIndex"
                    label="h指数"
                    :disabled="!editingD"
                    required
                  ></v-text-field>
                </v-col>
              </v-row>
              <v-row>
                <v-col cols="6">
                  <v-text-field
                    v-model="currentInst.id"
                    label="现工作单位id"
                    :disabled="!editingD"
                    required
                  ></v-text-field>
                </v-col>
                <v-col cols="6">
                  <v-text-field
                    v-model="currentInst.name"
                    label="现工作单位名称"
                    :disabled="!editingD"
                    required
                  ></v-text-field>
                </v-col>
              </v-row>
              <v-row v-for="i in institutions.length" :key="i">
                <v-col cols="5">
                  <v-text-field
                    v-model="institutions[i-1].id"
                    label="曾工作单位id"
                    :disabled="!editingD"
                    required
                  ></v-text-field>
                </v-col>
                <v-col cols="5">
                  <v-text-field
                    v-model="institutions[i-1].name"
                    label="曾工作单位名称"
                    :disabled="!editingD"
                    required
                  ></v-text-field>
                </v-col>
                <v-col cols="2" v-if="editingD">
                  <v-btn @click="deleteI(i-1)">
                    删除该项
                  </v-btn>
                </v-col>
              </v-row>
              <v-btn v-if="editingD" @click="addInst()">
                新增曾工作单位
              </v-btn>
              <v-row v-if="editingD">
                <v-col >
                  <v-text-field
                    v-model="websiteLink"
                    label="证明材料链接"
                  ></v-text-field>
                </v-col>
              </v-row>
              <v-row v-if="editingD">
                <v-col>
                  <v-file-input 
                  chips 
                  label="上传证明文件"
                  ></v-file-input>
                </v-col>
              </v-row>
              <v-row v-if="editingD">
                <v-col cols="6">
                  <v-text-field
                    v-model="vertifyCode"
                    label="验证码"
                    required
                  ></v-text-field>
                </v-col>
              </v-row>
              <v-row v-if="editingD">
                <v-col cols="6">
                  <v-btn v-if="editingD" @click="submit()">
                    提交修改信息
                  </v-btn>
                </v-col>
                <v-col cols="6">
                  <v-btn v-if="editingD" @click="editingD=false">
                    取消
                  </v-btn>
                </v-col>
              </v-row> 
          

            </div>-->
          </div>
        </v-col>




        <v-col v-if="manage==1" offset-md="1">
          <div style="padding-top:10px;">
            <v-btn @click="addingPaper=true">
                  新增文献
              </v-btn>
            <v-row
              v-for="i in onePageNum"
              :key="i"
            >
              <v-col cols="9">
                <PaperCard :item="papers[i+(page-1)*onePageNum-1]"></PaperCard>
              </v-col>
              <v-col>
                <br/>
                <br/>
                <v-btn @click="editPaper(i+(page-1)*onePageNum-1)">
                    编辑文献
                </v-btn>
                <v-btn @click="deletePaper(i+(page-1)*onePageNum-1)">
                    删除文献
                </v-btn>    
              </v-col>
            </v-row>
            <v-row>
              <v-col>
                <v-pagination
                  v-model="page"
                  class="my-4"
                  :length="8>pageNum?pageNum:8"
                ></v-pagination>
              </v-col>
            </v-row>
          </div>
          
        </v-col>
      </v-row>
    </div>
    <!-- 修改信息 -->
    <v-dialog v-model="editingD" persistent width=800px >
    <v-card>
      <div class="whole">
        <v-row>
          <v-col cols="10">
            <v-row>
              <v-col cols="9">
                <v-text-field
                  v-model="emailE"
                  label="邮箱"
                  :rules="emailRules"
                  required
                ></v-text-field>
              </v-col>
            </v-row>
            <v-row>
              <v-col cols="6">
                <v-text-field
                  v-model="nameE"
                  :rules="idRules"
                  label="姓名"
                  required
                ></v-text-field>
                </v-col>
                <v-col>
                <v-text-field
                  v-model="interestsE"
                  label="研究方向"
                  required
                ></v-text-field>
              </v-col>
            </v-row>
            <v-row>
              <v-col cols="6">
                <v-text-field
                  v-model="gIndexE"
                  label="g指数"
                  required
                ></v-text-field>
                </v-col>
                <v-col>
                <v-text-field
                  v-model="hIndexE"
                  label="h指数"
                  required
                ></v-text-field>
              </v-col>
            </v-row>
            <v-row>
              <v-col cols="6">
                <v-text-field
                  v-model="currentInstE.id"
                  label="现工作单位id"
                  required
                ></v-text-field>
              </v-col>
              <v-col cols="6">
                <v-text-field
                  v-model="currentInstE.name"
                  label="现工作单位名称"
                  required
                ></v-text-field>
              </v-col>
            </v-row>
            <v-row v-for="i in institutionsE.length" :key="i">
              <v-col cols="5">
                <v-text-field
                  v-model="institutionsE[i-1].id"
                  label="曾工作单位id"
                  required
                ></v-text-field>
              </v-col>
              <v-col cols="5">
                <v-text-field
                  v-model="institutionsE[i-1].name"
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
                <v-textarea
                  label="修改说明"
                  v-model="description"
                ></v-textarea>
              </v-col>
            </v-row>
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
            <v-row v-if="editingD">
              <v-col cols="6">
                <v-btn v-if="editingD" @click="submit()">
                  提交修改信息
                </v-btn>
              </v-col>
              <v-col cols="6">
                <v-btn v-if="editingD" @click="cancelAll()">
                  取消
                </v-btn>
              </v-col>
            </v-row>
          </v-col>
          <v-col>
            <div class="fixBut">
              <v-btn @click="getID=true">相关信息ID查询</v-btn>
            </div>  
          </v-col>
        </v-row>
         
      </div>
    </v-card>
    </v-dialog>
    <!-- 认领门户 -->
    <v-dialog v-model="addDoor" persistent width=600px >
      <v-card>
        <div class="whole">
          <v-row>
            <v-col cols="10">
              <v-row>
                <v-col>
                  <v-text-field
                    v-model="addDoorID"
                    label="新认领的门户id"
                    required
                  ></v-text-field>
                </v-col>
              </v-row>
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
                  <v-btn @click="submitAddDoor()">
                    提交认领信息
                  </v-btn>
                </v-col>
                <v-col cols="6">
                  <v-btn @click="cancelAll()">
                    取消
                  </v-btn>
                </v-col>
              </v-row> 
            </v-col>
            <v-col>
              <div class="fixBut">
                <v-btn @click="getID=true">相关信息ID查询</v-btn>
              </div>  
            </v-col>
          </v-row>
          
        </div>
        
      </v-card>
    </v-dialog>
    <!-- 删除文献 -->
    <v-dialog v-model="deletingPaper" persistent width=800px >
      <v-card>
        <div class="whole">
          <v-row>
            <v-col cols="10">
              <h2>移除文献:{{theDelete.title}}</h2>
              <v-row>
                <v-col >
                  <v-textarea
                    label="删除原因"
                    v-model="deletePaperReason"
                  ></v-textarea>
                </v-col>
              </v-row>
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
                  <v-btn @click="submitDeletePaper()">
                    提交认领信息
                  </v-btn>
                </v-col>
                <v-col cols="6">
                  <v-btn @click="cancelAll()">
                    取消
                  </v-btn>
                </v-col>
              </v-row> 
            </v-col>
            <v-col>
              <div class="fixBut">
                <v-btn @click="getID=true">相关信息ID查询</v-btn>
              </div>  
            </v-col>
            
          </v-row>
          
        </div>
        
      </v-card>
    </v-dialog>
    <!-- 新增文献 -->
    <v-dialog v-model="addingPaper" persistent width=800px>
      <BaseEditPaper 
      :paper="newPaper"
      :edit=false
      :email="email"
      @closeZ="closeF"
      ></BaseEditPaper>
    </v-dialog>
    <!-- 修改文献 -->
    <v-dialog v-model="editingPaper" persistent width=800px>
      <BaseEditPaper 
      :paper="thePaper"
      :edit=true
      :email="email"
      @closeZ="closeF"
      ></BaseEditPaper>
    </v-dialog>
    <!-- ID -->
    <v-dialog v-model="getID" persistent width=400px min-height=600px>
      <BaseGetID @closeID="closeID"></BaseGetID>
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
import Banner from '../components/BaseBanner.vue'
import PaperCard from '../components/BasePaperCard.vue'
import BaseEditPaper from './BaseEditPaper.vue'
import BaseGetID from './BaseGetID.vue'
  export default {
    components: {Banner,PaperCard,BaseEditPaper,BaseGetID},
    data: () => ({
      //门户部分
      editingD:false,
      addDoor:false,
      deletingPaper:false,
      editingPaper:false,
      addingPaper:false,
      getID:false,
      deletePaperID:"",
      deletePaperReason:"",
      manage:0,
      id:"",
      name:"",
      nameE:"",
      position:"",
      interests:"",
      interestsE:"",
      email:"",
      emailE:"",
      gIndex:"",
      hIndex:"",
      gIndexE:"",
      hIndexE:"",
      paperNum:0,
      patentNum:0,
      citationNum:0,
      avatarUrl:"",
      websiteLink:"",
      fileToken:"",
      vertifyCode:"",
      description:"",
      time:0,
      portals:[],
      addDoorID:"",
      snackbarEmail:false,
      snackbarSub:false,
      currentInst:{
        id:"",
        name:""
      },
      currentInstE:{
        id:"",
        name:""
      },
      institutions:[
        {
          id:"1",
          name:"a"
        },
        {
          id:"2",
          name:"b"
        }
      ],
      institutionsE:[],
      institutionNum:0,
      idRules: [(v) => !!v || "请填写姓名"],
      passwordRules: [(v) => !!v || "请填写密码"],
      emailRules: [
        (v) => !!v || "请填写邮箱",
        (v) => /.+@.+\..+/.test(v) || "邮箱格式不合法",
      ],
      
      //文献部分
      papers: [
        {
          "abstract": "假装这是一大段摘要",
          "authors": [
            {
              "id": "GF_4ynwBF-Mu8unTG1hc",
              "name": "谭火彬"
            }
          ],
          "citationNum": 114,
          "date": "2021-10-15",
          "id": "GF_4ynwBF-Mu8unTG1hc",
          "journal": {
            "id": "GF_4ynwBF-Mu8unTG1hc",
            "title": "Science"
          },
          "keywords": [12,23,34],
          "title": "基于机器学习的无需人工编制词典的切词系统",
          "type": "期刊论文"
        },
        {
          "abstract": "假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要",
          "authors": [
            {
              "id": "GF_4ynwBF-Mu8unTG1hc",
              "name": "谭火彬"
            }
          ],
          "citationNum": 114,
          "date": "2021-10-15",
          "doi":"1234123",
          institutions:[
            {
              id:"in1",
              name:"ins1"
            }
          ],
          publisher:"asda",
          referencePapers:[
            {
              id:"1",
              title:"rep1"
            }
          ],
          "subjects":["ma","ca","ph"],
          "id": "GF_4ynwBF-Mu8unTG1hd",
          "journal": {
            "id": "GF_4ynwBF-Mu8unTG1hc",
            "title": "Science"
          },
          "keywords": ["dasd","asdf","agagd","dasd"],
          "title": "基于机器学习的无需人工编制词典的切词系统",
          "type": "期刊论文"
        },
      ],
      newPaper:{
        title:"",
        id:"",
        abstract:"",
        authors:[],
        date:"",
        doi:"",//
        institutions:[],//
        journal:Object,
        keywords:[],
        publisher:"",//
        referencePapers:[],//
        subjects:[],//
        type:"",
        year:""
      },
      thePaper:Object,
      theDelete:"",
      page:1,
      onePageNum:1,
      pageNum:2,
    }),
    mounted(){
      // this.getInfo()
      // this.getPapers()
      this.nameE=this.name
      this.emailE=this.email
      this.interestsE=this.interests
      this.gIndexE=this.gIndex
      this.hIndexE=this.hIndex
      this.currentInstE=JSON.parse( JSON.stringify(this.currentInst) )
      this.institutionsE=JSON.parse( JSON.stringify(this.institutions) )
    },
    methods:{
      mDoor(){
        this.manage=0
      },
      mPaper(){
        this.manage=1
      },
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
        this.institutionsE.push({
          id:"",
          name:""
        })
      },
      addProtal(){
        this.portals.push("")
      },
      deleteI(index){
        this.institutionsE.splice(index, 1)
      },
      deleteP(index){
        this.portals.splice(index, 1)
      },
      deletePaper(index){
        this.deletingPaper=true
        this.theDelete=this.papers[index]
      },
      editPaper(index){
        this.editingPaper=true
        this.thePaper=this.papers[index]
      },
      closeF(msg){
        if(msg=="close"){
          this.addingPaper=false
          this.editingPaper=false
        }
      },
      closeID(msg){
        if(msg=="close")
          this.getID=false
      },
      cancelAll(){
        this.editingD=false,
        this.addDoor=false,
        this.deletingPaper=false,
        this.clearWeb()
      },
      clearWeb(){
        this.websiteLink="",
        this.vertifyCode="",
        this.fileToken=""
      },
      submit(){
        this.$axios({
          method: "post",
          url: "/api/scholar/modify",
          params: {
            ctfApp:{
              content:{
                description:this.description,
                info:{
                  currentInst:{
                    id:this.currentInstE.id,
                    name:this.currentInstE.name
                  },
                  gIndex:this.gIndexE,
                  hIndex:this.hIndexE,
                  institutions:this.institutionsE,
                  interests:this.interestsE,
                  name:this.nameE,
                }
              },
              email:this.emailE,
              fileToken:this.fileToken,
              websiteLink:this.websiteLink
            }
          }
        }).then(response => {
          console.log(response.data)
          this.snackbarSub=true
          this.editingD=false
          this.clearWeb()
        }).catch(error => {
          console.log(error)
        })
      },
      submitAddDoor(){
        this.$axios({
          method: "post",
          url: "/api/scholar/certify",
          params: {
            ctfApp:{
              content:{
                portals:this.portals,
              },  
              email:this.emailE,
              fileToken:this.fileToken,
              websiteLink:this.websiteLink
            }
          }
        }).then(response => {
          console.log(response.data)
          this.snackbarSub=true
          this.addDoor=false
          this.clearWeb()
        }).catch(error => {
          console.log(error)
        })
      },
      submitDeletePaper(){
        this.$axios({
          method: "post",
          url: "/api/scholar/paper/remove",
          params: {
            paper:{
              content:{
                paperId:this.deletePaperID,
                reason:this.deletePaperReason
              },
              email:this.emailE,
              fileToken:this.fileToken,
              websiteLink:this.websiteLink
            }
          }
        }).then(response => {
          console.log(response.data)
          this.snackbarSub=true
          this.deletingPaper=false
          this.clearWeb()
        }).catch(error => {
          console.log(error)
        })
      },
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
      },
      getPapers(){
        this.papers=[]
        let aPaper=Object
        let hasMore=false
        let page=0
        this.$axios({
          method: "get",
          url: "/search/relation/publications/researcher/"+this.id+"/"+page,
          params: {
            entity:"researcher",
            id: this.id,
            page:page,
          }
        }).then(response => {
          console.log(response.data)
          aPaper=response.data.item
          hasMore=response.data.hasMore
          this.papers.push(aPaper)
        }).catch(error => {
          console.log(error)
        })
        while (hasMore) {
            this.$axios({
            method: "get",
            url: "/search/relation/publications/researcher/"+this.id+"/"+page,
            params: {
              entity:"researcher",
              id: this.id,
              page:page,
            }
          }).then(response => {
            console.log(response.data)
            aPaper=response.data.item
            hasMore=response.data.hasMore
            this.papers.push(aPaper)
          }).catch(error => {
            console.log(error)
          })
        }
        this.pageNum=page+1
      }
    }
  }
</script>