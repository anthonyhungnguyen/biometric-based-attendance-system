import React, { lazy, Suspense } from "react"
import { Switch, Route } from "react-router-dom"
import Loading from "components/Loading"
import PATH from "constants/path"
const Login = lazy(() => import("pages/Login.jsx"))

export default function LoginRoute() {
  return (
    <Switch>
      <Route
        path={PATH.COMMON.LOGIN}
        component={() => (
          <Suspense fallback={<Loading />}>
            <Login />
          </Suspense>
        )}
      />
    </Switch>
  )
}
